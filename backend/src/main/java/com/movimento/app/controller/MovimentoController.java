package com.movimento.app.controller;

import com.movimento.app.dto.MovimentoRequest;
import com.movimento.app.dto.MovimentoResponse;
import com.movimento.app.model.Competencia;
import com.movimento.app.service.MovimentoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movimentos")
public class MovimentoController {
  private final MovimentoService service;

  public MovimentoController(MovimentoService service) {
    this.service = service;
  }

  @GetMapping
  public List<MovimentoResponse> consultar(@RequestParam int mes, @RequestParam int ano) {
    return service.consultar(new Competencia(mes, ano)).stream()
        .map(MovimentoResponse::from)
        .toList();
  }

  @PostMapping
  public ResponseEntity<MovimentoResponse> incluir(@Valid @RequestBody MovimentoRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(MovimentoResponse.from(service.incluir(request.toCommand())));
  }
}
