package com.movimento.cloudtest;

import static org.junit.jupiter.api.Assertions.*;

import com.movimento.app.MovimentoApplication;
import java.net.*;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;

class CloudConfigIntegrationTest {
  @Test
  void backendConsomeConfiguracaoDoServidorReal() throws Exception {
    try (var server =
        new SpringApplicationBuilder(ConfigFixture.class)
            .run(
                "--server.port=0",
                "--spring.profiles.active=native",
                "--spring.cloud.config.server.native.search-locations=classpath:/cloud-fixture/",
                "--spring.security.user.name=config",
                "--spring.security.user.password=Cloud-test-password-2026",
                "--spring.datasource.url=jdbc:h2:mem:config-fixture",
                "--spring.cloud.config.enabled=false",
                "--logging.level.root=WARN")) {
      int port = ((WebServerApplicationContext) server).getWebServer().getPort();
      try (var backend =
          new SpringApplicationBuilder(MovimentoApplication.class)
              .run(
                  "--server.port=0",
                  "--spring.cloud.config.enabled=true",
                  "--spring.profiles.active=cloud",
                  "--spring.config.import=configserver:",
                  "--spring.cloud.config.uri=http://127.0.0.1:" + port,
                  "--spring.cloud.config.username=config",
                  "--spring.cloud.config.password=Cloud-test-password-2026",
                  "--movimento.auth.password=Cloud-api-password-2026",
                  "--spring.datasource.url=jdbc:h2:mem:cloud-test;DB_CLOSE_DELAY=-1",
                  "--logging.level.root=WARN")) {
        assertEquals(
            "Configuração externa verificada",
            backend.getEnvironment().getProperty("movimento.message"));
        int backendPort = ((WebServerApplicationContext) backend).getWebServer().getPort();
        var client = HttpClient.newHttpClient();
        var login =
            client.send(
                HttpRequest.newBuilder(
                        URI.create("http://127.0.0.1:" + backendPort + "/api/v1/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            "{\"username\":\"admin\",\"password\":\"Cloud-api-password-2026\"}"))
                    .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, login.statusCode());
        String token =
            new tools.jackson.databind.ObjectMapper()
                .readTree(login.body())
                .get("accessToken")
                .asString();
        var info =
            client.send(
                HttpRequest.newBuilder(
                        URI.create("http://127.0.0.1:" + backendPort + "/api/v1/info"))
                    .header("Authorization", "Bearer " + token)
                    .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, info.statusCode());
        assertTrue(info.body().contains("Configuração externa verificada"));
      }
    }
  }

  @Test
  void perfilCloudFalhaQuandoServidorObrigatorioEstaIndisponivel() throws Exception {
    int unavailablePort;
    try (var socket = new java.net.ServerSocket(0)) {
      unavailablePort = socket.getLocalPort();
    }
    assertThrows(
        Exception.class,
        () -> {
          try (var ignored =
              new SpringApplicationBuilder(MovimentoApplication.class)
                  .run(
                      "--server.port=0",
                      "--spring.cloud.config.enabled=true",
                      "--spring.profiles.active=cloud",
                      "--spring.config.import=configserver:",
                      "--spring.cloud.config.uri=http://127.0.0.1:" + unavailablePort,
                      "--spring.cloud.config.password=Cloud-test-password-2026",
                      "--spring.cloud.config.request-connect-timeout=500",
                      "--movimento.auth.password=Cloud-api-password-2026",
                      "--logging.level.root=OFF")) {
            fail("Perfil cloud não deve iniciar sem o Config Server.");
          }
        });
  }
}
