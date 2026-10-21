package com.movimento.app.presentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.movimento.app.model.Competencia;
import com.movimento.app.model.Movimento;
import com.movimento.app.model.NovoMovimento;
import com.movimento.app.service.MovimentoService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired MovimentoService incluir;
  @Autowired JwtEncoder encoder;

  @BeforeEach
  void limpar() {
    jdbc.update("DELETE FROM MOVIMENTO_MANUAL");
  }

  private org.springframework.test.web.servlet.request.RequestPostProcessor autorizado() {
    return jwt()
        .authorities(createAuthorityList("SCOPE_movimentos.read", "SCOPE_movimentos.write"));
  }

  private String corpo(String produto, String cosif, String valor) {
    return "{\"mes\":2,\"ano\":2019,\"codigoProduto\":\""
        + produto
        + "\",\"codigoCosif\":\""
        + cosif
        + "\",\"descricao\":\"Teste integração\",\"valor\":\""
        + valor
        + "\"}";
  }

  @Test
  void exigeJwtEPermissao() throws Exception {
    mvc.perform(get("/api/v1/produtos")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/produtos").header("Authorization", "Bearer inválido"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/v1/movimentos")
                .with(jwt())
                .contentType("application/json")
                .content(corpo("P001", "11111111111", "1.00")))
        .andExpect(status().isForbidden());
  }

  @Test
  void loginEmiteTokenRealQueAbreRotaProtegida() throws Exception {
    String result =
        mvc.perform(
                post("/api/v1/auth/login")
                    .contentType("application/json")
                    .content(
                        "{\"username\":\"admin\",\"password\":\"Unit-test-only-credential-2026\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String token =
        new tools.jackson.databind.ObjectMapper().readTree(result).get("accessToken").asString();
    mvc.perform(get("/api/v1/produtos").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].codigo").value("P001"));
    mvc.perform(
            post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"errada\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void rejeitaTokenExpiradoEmissorEAudienceErrados() throws Exception {
    for (String erro : List.of("exp", "iss", "aud")) {
      Instant now = Instant.now();
      var claims =
          JwtClaimsSet.builder()
              .issuer(erro.equals("iss") ? "outro" : "movimento")
              .subject("admin")
              .audience(List.of(erro.equals("aud") ? "outra" : "movimento-api"))
              .issuedAt(now.minusSeconds(600))
              .expiresAt(erro.equals("exp") ? now.minusSeconds(120) : now.plusSeconds(600))
              .claim("scope", "movimentos.read")
              .build();
      String token =
          encoder
              .encode(
                  JwtEncoderParameters.from(
                      JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
              .getTokenValue();
      mvc.perform(get("/api/v1/produtos").header("Authorization", "Bearer " + token))
          .andExpect(status().isUnauthorized());
    }
  }

  @Test
  void insereConsultaEIsolaCompetencias() throws Exception {
    mvc.perform(
            post("/api/v1/movimentos")
                .with(autorizado())
                .contentType("application/json")
                .content(corpo("P001", "11111111111", "9999999999999999.99")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.numeroLancamento").value("1"))
        .andExpect(jsonPath("$.codigoUsuario").value("TESTE"))
        .andExpect(jsonPath("$.valor").value("9999999999999999.99"));
    mvc.perform(
            post("/api/v1/movimentos")
                .with(autorizado())
                .contentType("application/json")
                .content(corpo("P002", "22222222222", "-12.30")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.numeroLancamento").value("2"));
    mvc.perform(get("/api/v1/movimentos?mes=2&ano=2019").with(autorizado()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].descricaoProduto").value("Renda fixa"));
    mvc.perform(get("/api/v1/movimentos?mes=3&ano=2019").with(autorizado()))
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void rejeitaCamposGeradosEntradaInvalidaEVinculoIncompativel() throws Exception {
    String valid = corpo("P001", "11111111111", "1.00");
    mvc.perform(
            post("/api/v1/movimentos")
                .with(autorizado())
                .contentType("application/json")
                .content(valid.replace("}", ",\"codigoUsuario\":\"OUTRO\"}")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/movimentos")
                .with(autorizado())
                .contentType("application/json")
                .content(corpo("P001", "11111111111", "1.001")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.valor").exists());
    mvc.perform(
            post("/api/v1/movimentos")
                .with(autorizado())
                .contentType("application/json")
                .content(corpo("P001", "22222222222", "1.00")))
        .andExpect(status().isUnprocessableEntity());
    mvc.perform(
            post("/api/v1/movimentos")
                .with(autorizado())
                .contentType("application/json")
                .content(corpo("XXXX", "11111111111", "1.00")))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/movimentos?mes=0&ano=2019").with(autorizado()))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/movimentos?mes=1").with(autorizado()))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/produtos/XXXX/cosifs").with(autorizado()))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/produtos/P001/cosifs").with(autorizado()))
        .andExpect(jsonPath("$.length()").value(2));
    assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM MOVIMENTO_MANUAL", Integer.class));
  }

  @Test
  void sequenciaConcorrenteEntreProdutosEmCompetenciaVaziaEExistente() throws Exception {
    for (int rodada = 0; rodada < 2; rodada++) {
      try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Movimento>> futures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
          final boolean outro = i % 2 == 0;
          futures.add(
              pool.submit(
                  () -> {
                    start.await();
                    return incluir.incluir(
                        new NovoMovimento(
                            new Competencia(6, 2026),
                            outro ? "P001" : "P002",
                            outro ? "11111111111" : "22222222222",
                            "Concorrente",
                            new BigDecimal("10.01")));
                  }));
        }
        start.countDown();
        Set<Long> numeros = new HashSet<>();
        for (var f : futures) numeros.add(f.get(30, TimeUnit.SECONDS).numeroLancamento());
        assertEquals(4, numeros.size());
      }
    }
    assertEquals(
        8,
        jdbc.queryForObject(
            "SELECT COUNT(DISTINCT NUM_LANCAMENTO) FROM MOVIMENTO_MANUAL", Integer.class));
  }

  @Test
  void bancoImpedeRelacionamentoInvalidoENumeroDuplicado() {
    incluir.incluir(
        new NovoMovimento(
            new Competencia(2, 2019), "P001", "11111111111", "Original", BigDecimal.ONE));
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () ->
            jdbc.update(
                "INSERT INTO MOVIMENTO_MANUAL VALUES (2,2019,1,'P002','22222222222','Duplicado',CURRENT_TIMESTAMP,'TESTE',1.00)"));
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () ->
            jdbc.update(
                "INSERT INTO MOVIMENTO_MANUAL VALUES (2,2019,2,'P001','22222222222','FK inválida',CURRENT_TIMESTAMP,'TESTE',1.00)"));
  }

  @Test
  void rejeitaLetrasEFracoesNosCamposNumericos() throws Exception {
    String valid = corpo("P001", "11111111111", "1.00");
    for (String body :
        java.util.List.of(
            valid.replace("\"mes\":2", "\"mes\":\"abc\""),
            valid.replace("\"ano\":2019", "\"ano\":\"abc\""),
            valid.replace("\"mes\":2", "\"mes\":2.5"),
            valid.replace("\"ano\":2019", "\"ano\":2019.5"),
            corpo("P001", "11111111111", "1e3"),
            corpo("P001", "11111111111", "abc"))) {
      mvc.perform(
              post("/api/v1/movimentos")
                  .with(autorizado())
                  .contentType("application/json")
                  .content(body))
          .andExpect(status().isBadRequest());
    }
    assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM MOVIMENTO_MANUAL", Integer.class));
  }
}
