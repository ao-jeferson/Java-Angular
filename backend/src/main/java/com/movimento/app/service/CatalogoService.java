package com.movimento.app.service;

import com.movimento.app.dto.Cosif;
import com.movimento.app.dto.Produto;
import com.movimento.app.exception.RecursoNaoEncontradoException;
import com.movimento.app.repository.CosifRepository;
import com.movimento.app.repository.ProdutoRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogoService {
  private final ProdutoRepository produtos;
  private final CosifRepository cosifs;

  public CatalogoService(ProdutoRepository produtos, CosifRepository cosifs) {
    this.produtos = produtos;
    this.cosifs = cosifs;
  }

  public List<Produto> produtos() {
    return produtos.findAll(Sort.by("codigo")).stream()
        .map(p -> new Produto(p.codigo.strip(), p.descricao))
        .toList();
  }

  public List<Cosif> cosifs(String codigo) {
    if (!produtos.existsById(codigo))
      throw new RecursoNaoEncontradoException("Produto não encontrado.");
    return cosifs.findByIdProdutoOrderByIdCosif(codigo).stream()
        .map(
            c ->
                new Cosif(
                    c.id.cosif.strip(), c.classificacao == null ? null : c.classificacao.strip()))
        .toList();
  }
}
