package br.com.antlia.movimento.infrastructure.persistence.entity;

import jakarta.persistence.*;
@Entity @Table(name="PRODUTO")
public class ProdutoEntity {
    @Id @Column(name="COD_PRODUTO", columnDefinition="char(4)") public String codigo;
    @Column(name="DES_PRODUTO", length=30) public String descricao;
    @Column(name="STA_STATUS", columnDefinition="char(1)") public String status;
    protected ProdutoEntity() {}
}
