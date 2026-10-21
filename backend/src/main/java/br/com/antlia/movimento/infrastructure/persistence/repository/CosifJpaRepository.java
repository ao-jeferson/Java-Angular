package br.com.antlia.movimento.infrastructure.persistence.repository;

import br.com.antlia.movimento.infrastructure.persistence.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CosifJpaRepository extends JpaRepository<CosifEntity,CosifId> {
    List<CosifEntity> findByIdProdutoOrderByIdCosif(String produto);
}
