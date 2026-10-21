package br.com.antlia.movimento.service;

import br.com.antlia.movimento.exception.RecursoNaoEncontradoException;
import br.com.antlia.movimento.exception.RegraNegocioException;
import br.com.antlia.movimento.model.Competencia;
import br.com.antlia.movimento.model.CosifId;
import br.com.antlia.movimento.model.Movimento;
import br.com.antlia.movimento.model.MovimentoEntity;
import br.com.antlia.movimento.model.MovimentoId;
import br.com.antlia.movimento.model.NovoMovimento;
import br.com.antlia.movimento.model.ProdutoEntity;
import br.com.antlia.movimento.repository.CosifRepository;
import br.com.antlia.movimento.repository.MovimentoRepository;
import br.com.antlia.movimento.repository.ProdutoRepository;
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
