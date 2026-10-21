package br.com.antlia.movimento.infrastructure.persistence.adapter;

import br.com.antlia.movimento.domain.*;
import br.com.antlia.movimento.infrastructure.persistence.entity.CosifId;
import br.com.antlia.movimento.infrastructure.persistence.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import java.util.*;
@Repository
public class CatalogoJpaAdapter implements CatalogoRepository {
    private final ProdutoJpaRepository produtos;
    private final CosifJpaRepository cosifs;
    public CatalogoJpaAdapter(ProdutoJpaRepository produtos,CosifJpaRepository cosifs) { this.produtos=produtos; this.cosifs=cosifs; }
    public List<Produto> produtos() { return produtos.findAll(Sort.by("codigo")).stream().map(p -> new Produto(p.codigo.strip(),p.descricao)).toList(); }
    public Optional<Produto> produto(String codigo) { return produtos.findById(codigo).map(p -> new Produto(p.codigo.strip(),p.descricao)); }
    public List<Cosif> cosifs(String produto) { return cosifs.findByIdProdutoOrderByIdCosif(produto).stream().map(c -> new Cosif(c.id.cosif.strip(),c.classificacao == null ? null : c.classificacao.strip())).toList(); }
    public boolean possuiCosif(String produto,String cosif) { return cosifs.existsById(new CosifId(produto,cosif)); }
}
