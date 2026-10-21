package br.com.antlia.movimento.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class InfoController {
  private final String message;

  public InfoController(@Value("${movimento.message}") String message) {
    this.message = message;
  }

  @GetMapping("/api/v1/info")
  public Map<String, String> info() {
    return Map.of("nome", "Movimento", "ambiente", message);
  }
}
