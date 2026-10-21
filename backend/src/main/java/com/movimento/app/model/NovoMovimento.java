package com.movimento.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record NovoMovimento(
    Competencia competencia,
    String codigoProduto,
    String codigoCosif,
    String descricao,
    BigDecimal valor) {
  public NovoMovimento {
    Objects.requireNonNull(competencia, "Competência obrigatória.");
    codigoProduto = texto(codigoProduto, 4, "Produto");
    codigoCosif = texto(codigoCosif, 11, "Cosif");
    descricao = texto(descricao, 50, "Descrição");
    if (valor == null) throw new IllegalArgumentException("Valor obrigatório.");
    try {
      valor = valor.setScale(2, RoundingMode.UNNECESSARY);
    } catch (ArithmeticException ex) {
      throw new IllegalArgumentException("Valor aceita até duas casas decimais.");
    }
    if (valor.precision() > 18) throw new IllegalArgumentException("Valor excede NUMERIC(18,2).");
  }

  private static String texto(String valor, int max, String nome) {
    if (valor == null || valor.isBlank() || valor.length() > max)
      throw new IllegalArgumentException(
          nome + " é obrigatório e aceita até " + max + " caracteres.");
    return valor.strip();
  }
}
