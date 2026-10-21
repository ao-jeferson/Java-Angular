package br.com.antlia.movimento.model;

public record Competencia(int mes, int ano) {
  public Competencia {
    if (mes < 1 || mes > 12) throw new IllegalArgumentException("Mês deve estar entre 1 e 12.");
    if (ano < 1 || ano > 9999) throw new IllegalArgumentException("Ano deve estar entre 1 e 9999.");
  }
}
