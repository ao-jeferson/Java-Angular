package br.com.antlia.movimento.application;

import java.util.function.Supplier;
public interface Transacao { <T> T executar(Supplier<T> acao); }
