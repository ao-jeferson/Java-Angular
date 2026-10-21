package br.com.antlia.movimento.presentation.rest;

import br.com.antlia.movimento.infrastructure.security.AutenticacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AutenticacaoService service;
    public AuthController(AutenticacaoService service) { this.service=service; }
    @PostMapping("/login")
    public ResponseEntity<AutenticacaoService.Token> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.autenticar(request.username(),request.password()));
    }
    public record LoginRequest(@NotBlank @Size(max=100) String username,@NotBlank @Size(max=72) String password) {}
}
