package br.com.antlia.movimento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PRODUTO_COSIF")
public class CosifEntity {
  @EmbeddedId public CosifId id;

  @Column(name = "COD_CLASSIFICACAO", columnDefinition = "char(6)")
  public String classificacao;

  @Column(name = "STA_STATUS", columnDefinition = "char(1)")
  public String status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "COD_PRODUTO", insertable = false, updatable = false)
  public ProdutoEntity produto;

  protected CosifEntity() {}
}
