package br.com.antlia.movimento.application;

import br.com.antlia.movimento.domain.*;
import java.util.List;
public final class ConsultarMovimentos {
    private final MovimentoRepository repository;
    public ConsultarMovimentos(MovimentoRepository repository) { this.repository = repository; }
    public List<Movimento> executar(Competencia competencia) { return repository.consultar(competencia); }
}
