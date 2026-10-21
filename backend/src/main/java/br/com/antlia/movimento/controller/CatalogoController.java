package br.com.antlia.movimento.controller;

import br.com.antlia.movimento.dto.Cosif;
import br.com.antlia.movimento.dto.Produto;
import br.com.antlia.movimento.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/produtos")
public class CatalogoController {
  private final CatalogoService catalogo;

  public CatalogoController(CatalogoService catalogo) {
    this.catalogo = catalogo;
  }

  @GetMapping
  public List<Produto> produtos() {
    return catalogo.produtos();
  }

  @GetMapping("/{codigo}/cosifs")
  public List<Cosif> cosifs(@PathVariable String codigo) {
    return catalogo.cosifs(codigo);
  }
}
