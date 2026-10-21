package br.com.antlia.movimento.domain;

public class ConflitoNumeracaoException extends RuntimeException {
    public ConflitoNumeracaoException() { super("Concorrência na numeração. Tente novamente."); }
}
