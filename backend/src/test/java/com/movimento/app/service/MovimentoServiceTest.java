package com.movimento.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.movimento.app.exception.RecursoNaoEncontradoException;
import com.movimento.app.exception.RegraNegocioException;
import com.movimento.app.model.Competencia;
import com.movimento.app.model.NovoMovimento;
import com.movimento.app.model.ProdutoEntity;
import com.movimento.app.repository.CosifRepository;
import com.movimento.app.repository.MovimentoRepository;
import com.movimento.app.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;

class MovimentoServiceTest {
  MovimentoRepository movimentos = mock(MovimentoRepository.class);
  ProdutoRepository produtos = mock(ProdutoRepository.class);
  CosifRepository cosifs = mock(CosifRepository.class);
  Clock clock = Clock.fixed(Instant.parse("2026-10-21T01:00:00Z"), ZoneId.of("America/Sao_Paulo"));
  MovimentoService service = new MovimentoService(movimentos, produtos, cosifs, clock);
  NovoMovimento comando =
      new NovoMovimento(
          new Competencia(2, 2019), "P001", "11111111111", "Teste", new BigDecimal("150.00"));

  @BeforeEach
  void preparar() {
    ProdutoEntity produto = new ProdutoEntity();
    produto.codigo = "P001";
    produto.descricao = "Renda fixa";
    when(produtos.findById("P001")).thenReturn(Optional.of(produto));
    when(cosifs.existsById(any())).thenReturn(true);
    when(movimentos.ultimo(any(), any())).thenReturn(BigDecimal.ZERO);
  }

  @Test
  void numeroDataEUsuarioGeradosNoServidor() {
    var m = service.incluir(comando);
    assertEquals(1, m.numeroLancamento());
    assertEquals("TESTE", m.codigoUsuario());
    assertEquals(LocalDateTime.of(2026, 10, 20, 22, 0), m.dataMovimento());
    assertEquals(new BigDecimal("150.00"), m.valor());
    var order = inOrder(produtos, movimentos);
    order.verify(produtos).bloquearCatalogo();
    order.verify(produtos).findById("P001");
    order.verify(movimentos).ultimo(any(), any());
    order.verify(movimentos).saveAndFlush(any());
  }

  @Test
  void incrementaSequenciaDaCompetencia() {
    when(movimentos.ultimo(any(), any())).thenReturn(BigDecimal.valueOf(27));
    assertEquals(28, service.incluir(comando).numeroLancamento());
  }

  @Test
  void rejeitaProdutoInexistente() {
    when(produtos.findById("P001")).thenReturn(Optional.empty());
    assertThrows(RecursoNaoEncontradoException.class, () -> service.incluir(comando));
    verify(movimentos, never()).saveAndFlush(any());
  }

  @Test
  void rejeitaCosifDeOutroProduto() {
    when(cosifs.existsById(any())).thenReturn(false);
    assertThrows(RegraNegocioException.class, () -> service.incluir(comando));
    verify(movimentos, never()).saveAndFlush(any());
  }

  @Test
  void rejeitaOverflow() {
    when(movimentos.ultimo(any(), any())).thenReturn(new BigDecimal("999999999999999999"));
    assertThrows(RegraNegocioException.class, () -> service.incluir(comando));
  }

  @Test
  void propagaFalhaDePersistencia() {
    when(movimentos.saveAndFlush(any())).thenThrow(new IllegalStateException("Falha"));
    assertThrows(IllegalStateException.class, () -> service.incluir(comando));
  }
}
