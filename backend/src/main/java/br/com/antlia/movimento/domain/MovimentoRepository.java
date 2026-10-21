package br.com.antlia.movimento.domain;

import java.util.List;
public interface MovimentoRepository {
    long ultimoNumero(Competencia competencia);
    Movimento salvar(Movimento movimento);
    List<Movimento> consultar(Competencia competencia);
}
