package br.com.antlia.movimento.application;

import br.com.antlia.movimento.domain.*;
import java.util.List;
public final class ConsultarCatalogo {
    private final CatalogoRepository repository;
    public ConsultarCatalogo(CatalogoRepository repository) { this.repository = repository; }
    public List<Produto> produtos() { return repository.produtos(); }
    public List<Cosif> cosifs(String codigo) {
        repository.produto(codigo).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
        return repository.cosifs(codigo);
    }
}
