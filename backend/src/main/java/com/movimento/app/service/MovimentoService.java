package com.movimento.app.service;

import com.movimento.app.exception.RecursoNaoEncontradoException;
import com.movimento.app.exception.RegraNegocioException;
import com.movimento.app.model.Competencia;
import com.movimento.app.model.CosifId;
import com.movimento.app.model.Movimento;
import com.movimento.app.model.MovimentoEntity;
import com.movimento.app.model.MovimentoId;
import com.movimento.app.model.NovoMovimento;
import com.movimento.app.model.ProdutoEntity;
import com.movimento.app.repository.CosifRepository;
import com.movimento.app.repository.MovimentoRepository;
import com.movimento.app.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentoService {
  private final MovimentoRepository movimentos;
  private final ProdutoRepository produtos;
  private final CosifRepository cosifs;
  private final Clock clock;

  public MovimentoService(
      MovimentoRepository movimentos,
      ProdutoRepository produtos,
      CosifRepository cosifs,
      Clock clock) {
    this.movimentos = movimentos;
    this.produtos = produtos;
    this.cosifs = cosifs;
    this.clock = clock;
  }

  @Transactional
  public Movimento incluir(NovoMovimento comando) {
    // A single database lock serializes numbering, including an empty month/year.
    produtos.bloquearCatalogo();
    ProdutoEntity produto =
        produtos
            .findById(comando.codigoProduto())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    if (!cosifs.existsById(new CosifId(comando.codigoProduto(), comando.codigoCosif())))
      throw new RegraNegocioException("Cosif não pertence ao produto informado.");
    Competencia c = comando.competencia();
    long numero =
        movimentos.ultimo(BigDecimal.valueOf(c.mes()), BigDecimal.valueOf(c.ano())).longValueExact()
            + 1;
    if (numero > 999999999999999999L)
      throw new RegraNegocioException("Limite de lançamentos da competência atingido.");
    MovimentoEntity entity = new MovimentoEntity();
    entity.id =
        new MovimentoId(c.mes(), c.ano(), numero, comando.codigoProduto(), comando.codigoCosif());
    entity.descricao = comando.descricao();
    entity.valor = comando.valor();
    entity.data = LocalDateTime.now(clock);
    entity.usuario = "TESTE";
    movimentos.saveAndFlush(entity);
    return new Movimento(
        c,
        numero,
        comando.codigoProduto(),
        comando.codigoCosif(),
        produto.descricao,
        entity.descricao,
        entity.valor,
        entity.data,
        entity.usuario);
  }

  @Transactional(readOnly = true)
  public List<Movimento> consultar(Competencia c) {
    return movimentos.consultar(BigDecimal.valueOf(c.mes()), BigDecimal.valueOf(c.ano())).stream()
        .map(
            e ->
                new Movimento(
                    c,
                    e.id.numero.longValueExact(),
                    e.id.produto.strip(),
                    e.id.cosif.strip(),
                    e.cosif.produto.descricao,
                    e.descricao,
                    e.valor,
                    e.data,
                    e.usuario))
        .toList();
  }
}
