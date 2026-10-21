package br.com.antlia.movimento.domain;

import java.util.List;
import java.util.Optional;
public interface CatalogoRepository {
    List<Produto> produtos();
    Optional<Produto> produto(String codigo);
    List<Cosif> cosifs(String codigoProduto);
    boolean possuiCosif(String codigoProduto, String codigoCosif);
}
