package br.com.antlia.movimento.controller;

import br.com.antlia.movimento.dto.MovimentoRequest;
import br.com.antlia.movimento.dto.MovimentoResponse;
import br.com.antlia.movimento.model.Competencia;
import br.com.antlia.movimento.service.MovimentoService;
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
