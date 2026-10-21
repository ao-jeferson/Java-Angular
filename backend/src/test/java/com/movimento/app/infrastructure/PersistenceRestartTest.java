package com.movimento.app.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.movimento.app.MovimentoApplication;
import com.movimento.app.model.Competencia;
import com.movimento.app.model.NovoMovimento;
import com.movimento.app.service.MovimentoService;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;

class PersistenceRestartTest {
  @TempDir Path directory;

  @Test
  void dadosSobrevivemAoReinicioComH2EmArquivo() {
    String[] properties = {
      "--server.port=0",
      "--movimento.auth.password=Restart-test-password-2026",
      "--spring.cloud.config.enabled=false",
      "--spring.datasource.url=jdbc:h2:file:"
          + directory.resolve("movimento")
          + ";DB_CLOSE_ON_EXIT=FALSE",
      "--logging.level.root=WARN"
    };
    try (var first = new SpringApplicationBuilder(MovimentoApplication.class).run(properties)) {
      first
          .getBean(MovimentoService.class)
          .incluir(
              new NovoMovimento(
                  new Competencia(1, 2026),
                  "P001",
                  "11111111111",
                  "Persistência",
                  new BigDecimal("123.45")));
    }
    try (var second = new SpringApplicationBuilder(MovimentoApplication.class).run(properties)) {
      var rows = second.getBean(MovimentoService.class).consultar(new Competencia(1, 2026));
      assertEquals(1, rows.size());
      assertEquals(new BigDecimal("123.45"), rows.getFirst().valor());
      assertEquals(
          2,
          second
              .getBean(MovimentoService.class)
              .incluir(
                  new NovoMovimento(
                      new Competencia(1, 2026),
                      "P002",
                      "22222222222",
                      "Após reinício",
                      BigDecimal.ONE))
              .numeroLancamento());
    }
  }
}
