package br.com.antlia.movimento.infrastructure.persistence.adapter;

import br.com.antlia.movimento.domain.*;
import br.com.antlia.movimento.infrastructure.persistence.entity.*;
import br.com.antlia.movimento.infrastructure.persistence.repository.MovimentoJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
@Repository
public class MovimentoJpaAdapter implements MovimentoRepository {
    private final MovimentoJpaRepository repository;
    private final EntityManager entityManager;
    public MovimentoJpaAdapter(MovimentoJpaRepository repository,EntityManager entityManager) { this.repository=repository; this.entityManager=entityManager; }
    public long ultimoNumero(Competencia c) { return repository.ultimo(BigDecimal.valueOf(c.mes()),BigDecimal.valueOf(c.ano())).longValueExact(); }
    public Movimento salvar(Movimento m) {
        MovimentoEntity e=new MovimentoEntity();
        e.id=new MovimentoId(m.competencia().mes(),m.competencia().ano(),m.numeroLancamento(),m.codigoProduto(),m.codigoCosif());
        e.descricao=m.descricao(); e.valor=m.valor(); e.data=m.dataMovimento(); e.usuario=m.codigoUsuario();
        entityManager.persist(e); entityManager.flush();
        return m;
    }
    @Transactional(readOnly=true)
    public List<Movimento> consultar(Competencia c) {
        return repository.consultar(BigDecimal.valueOf(c.mes()),BigDecimal.valueOf(c.ano())).stream().map(e ->
            new Movimento(c,e.id.numero.longValueExact(),e.id.produto.strip(),e.id.cosif.strip(),e.cosif.produto.descricao,e.descricao,e.valor,e.data,e.usuario)).toList();
    }
}
