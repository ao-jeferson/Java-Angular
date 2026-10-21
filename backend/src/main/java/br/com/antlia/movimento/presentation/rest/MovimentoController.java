package br.com.antlia.movimento.presentation.rest;

import br.com.antlia.movimento.application.*;
import br.com.antlia.movimento.domain.Competencia;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/movimentos")
public class MovimentoController {
    private final IncluirMovimento incluir;
    private final ConsultarMovimentos consultar;
    public MovimentoController(IncluirMovimento incluir,ConsultarMovimentos consultar) { this.incluir=incluir; this.consultar=consultar; }
    @GetMapping public List<MovimentoResponse> consultar(@RequestParam int mes,@RequestParam int ano) {
        return consultar.executar(new Competencia(mes,ano)).stream().map(MovimentoResponse::from).toList();
    }
    @PostMapping public ResponseEntity<MovimentoResponse> incluir(@Valid @RequestBody MovimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(MovimentoResponse.from(incluir.executar(request.toCommand())));
    }
}
