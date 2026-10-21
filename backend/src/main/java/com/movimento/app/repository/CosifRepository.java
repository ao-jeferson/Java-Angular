package com.movimento.app.repository;

import com.movimento.app.model.CosifEntity;
import com.movimento.app.model.CosifId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CosifRepository extends JpaRepository<CosifEntity, CosifId> {
  List<CosifEntity> findByIdProdutoOrderByIdCosif(String produto);
}
