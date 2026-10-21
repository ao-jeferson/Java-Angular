package br.com.antlia.movimento.presentation.rest;

import br.com.antlia.movimento.application.ConsultarCatalogo;
import br.com.antlia.movimento.domain.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/produtos")
public class CatalogoController {
    private final ConsultarCatalogo catalogo;
    public CatalogoController(ConsultarCatalogo catalogo) { this.catalogo=catalogo; }
    @GetMapping public List<Produto> produtos() { return catalogo.produtos(); }
    @GetMapping("/{codigo}/cosifs") public List<Cosif> cosifs(@PathVariable String codigo) { return catalogo.cosifs(codigo); }
}
