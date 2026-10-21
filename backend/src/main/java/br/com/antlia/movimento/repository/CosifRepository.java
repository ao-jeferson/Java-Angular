package br.com.antlia.movimento.repository;

import br.com.antlia.movimento.model.CosifEntity;
import br.com.antlia.movimento.model.CosifId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CosifRepository extends JpaRepository<CosifEntity, CosifId> {
  List<CosifEntity> findByIdProdutoOrderByIdCosif(String produto);
}
