package br.com.antlia.movimento.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class MovimentoId implements Serializable {
  @Column(name = "DAT_MES", precision = 2, scale = 0)
  public BigDecimal mes;

  @Column(name = "DAT_ANO", precision = 4, scale = 0)
  public BigDecimal ano;

  @Column(name = "NUM_LANCAMENTO", precision = 18, scale = 0)
  public BigDecimal numero;

  @Column(name = "COD_PRODUTO", columnDefinition = "char(4)")
  public String produto;

  @Column(name = "COD_COSIF", columnDefinition = "char(11)")
  public String cosif;

  protected MovimentoId() {}

  public MovimentoId(int mes, int ano, long numero, String produto, String cosif) {
    this.mes = BigDecimal.valueOf(mes);
    this.ano = BigDecimal.valueOf(ano);
    this.numero = BigDecimal.valueOf(numero);
    this.produto = produto;
    this.cosif = cosif;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MovimentoId id
        && Objects.equals(mes, id.mes)
        && Objects.equals(ano, id.ano)
        && Objects.equals(numero, id.numero)
        && Objects.equals(produto, id.produto)
        && Objects.equals(cosif, id.cosif);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mes, ano, numero, produto, cosif);
  }
}
