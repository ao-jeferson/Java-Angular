package com.movimento.app.controller;

import com.movimento.app.dto.Cosif;
import com.movimento.app.dto.Produto;
import com.movimento.app.service.CatalogoService;
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
