package com.movimento.app.repository;

import com.movimento.app.model.MovimentoEntity;
import com.movimento.app.model.MovimentoId;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MovimentoRepository extends JpaRepository<MovimentoEntity, MovimentoId> {
  @Query(
      "select coalesce(max(m.id.numero),0) from MovimentoEntity m where m.id.mes=:mes and m.id.ano=:ano")
  BigDecimal ultimo(@Param("mes") BigDecimal mes, @Param("ano") BigDecimal ano);

  @Query(
      "select m from MovimentoEntity m join fetch m.cosif c join fetch c.produto where m.id.mes=:mes and m.id.ano=:ano order by m.id.numero")
  List<MovimentoEntity> consultar(@Param("mes") BigDecimal mes, @Param("ano") BigDecimal ano);
}
