package com.movimento.app.repository;

import com.movimento.app.model.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, String> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select p from ProdutoEntity p order by p.codigo")
  java.util.List<ProdutoEntity> bloquearCatalogo();
}
