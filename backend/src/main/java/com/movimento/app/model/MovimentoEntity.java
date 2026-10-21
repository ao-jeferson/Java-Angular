package com.movimento.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMENTO_MANUAL")
public class MovimentoEntity {
  @EmbeddedId public MovimentoId id;

  @Column(name = "DES_DESCRICAO", length = 50, nullable = false)
  public String descricao;

  @Column(name = "VAL_VALOR", precision = 18, scale = 2, nullable = false)
  public BigDecimal valor;

  @Column(name = "DAT_MOVIMENTO", nullable = false)
  public LocalDateTime data;

  @Column(name = "COD_USUARIO", length = 15, nullable = false)
  public String usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
    @JoinColumn(
        name = "COD_PRODUTO",
        referencedColumnName = "COD_PRODUTO",
        insertable = false,
        updatable = false),
    @JoinColumn(
        name = "COD_COSIF",
        referencedColumnName = "COD_COSIF",
        insertable = false,
        updatable = false)
  })
  public CosifEntity cosif;

  public MovimentoEntity() {}
}
