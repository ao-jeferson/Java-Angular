package br.com.antlia.movimento.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
@Embeddable
public class CosifId implements Serializable {
    @Column(name="COD_PRODUTO", columnDefinition="char(4)") public String produto;
    @Column(name="COD_COSIF", columnDefinition="char(11)") public String cosif;
    protected CosifId() {}
    public CosifId(String produto, String cosif) { this.produto = produto; this.cosif = cosif; }
    @Override public boolean equals(Object o) { return o instanceof CosifId id && Objects.equals(produto,id.produto) && Objects.equals(cosif,id.cosif); }
    @Override public int hashCode() { return Objects.hash(produto,cosif); }
}
