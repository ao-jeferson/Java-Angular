package br.com.antlia.movimento.infrastructure.persistence.repository;

import br.com.antlia.movimento.infrastructure.persistence.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity,String> {}
