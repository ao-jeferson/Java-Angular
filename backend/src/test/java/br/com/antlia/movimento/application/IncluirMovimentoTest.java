package br.com.antlia.movimento.application;

import br.com.antlia.movimento.domain.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class IncluirMovimentoTest {
 private final MovimentoRepository movimentos=mock(MovimentoRepository.class);
 private final CatalogoRepository catalogo=mock(CatalogoRepository.class);
 private final Competencia competencia=new Competencia(2,2019);
 private final NovoMovimento comando=new NovoMovimento(competencia,"P001","11111111111","Teste",new BigDecimal("150.00"));
 private final Clock clock=Clock.fixed(Instant.parse("2026-10-21T01:00:00Z"),ZoneId.of("America/Sao_Paulo"));
 private final Transacao transacao=new Transacao() { public <T>T executar(Supplier<T> acao) { return acao.get(); } };
 @BeforeEach void preparar() {
  when(catalogo.produto("P001")).thenReturn(Optional.of(new Produto("P001","Renda fixa")));
  when(catalogo.possuiCosif("P001","11111111111")).thenReturn(true);
  when(movimentos.salvar(any())).thenAnswer(i->i.getArgument(0));
 }
 @Test void geraPrimeiroNumeroDataEUsuario() {
  Movimento m=service(transacao).executar(comando);
  assertEquals(1,m.numeroLancamento()); assertEquals("TESTE",m.codigoUsuario());
  assertEquals(LocalDateTime.of(2026,10,20,22,0),m.dataMovimento());
  assertEquals(new BigDecimal("150.00"),m.valor());
 }
 @Test void incrementaUltimoNumeroDaCompetencia() {
  when(movimentos.ultimoNumero(competencia)).thenReturn(27L);
  assertEquals(28,service(transacao).executar(comando).numeroLancamento());
 }
 @Test void rejeitaProdutoInexistenteSemGravar() {
  when(catalogo.produto("P001")).thenReturn(Optional.empty());
  assertThrows(RecursoNaoEncontradoException.class,()->service(transacao).executar(comando)); verify(movimentos,never()).salvar(any());
 }
 @Test void rejeitaCosifDeOutroProdutoSemGravar() {
  when(catalogo.possuiCosif(anyString(),anyString())).thenReturn(false);
  assertThrows(RegraNegocioException.class,()->service(transacao).executar(comando)); verify(movimentos,never()).salvar(any());
 }
 @Test void rejeitaOverflow() {
  when(movimentos.ultimoNumero(competencia)).thenReturn(999999999999999999L);
  assertThrows(RegraNegocioException.class,()->service(transacao).executar(comando));
 }
 @Test void refazSomenteConflitoDeNumeracao() {
  when(movimentos.salvar(any())).thenThrow(new ConflitoNumeracaoException()).thenAnswer(i->i.getArgument(0));
  assertEquals(1,service(transacao).executar(comando).numeroLancamento()); verify(movimentos,times(2)).ultimoNumero(competencia);
 }
 @Test void limitaTentativas() {
  when(movimentos.salvar(any())).thenThrow(new ConflitoNumeracaoException());
  assertThrows(ConflitoNumeracaoException.class,()->service(transacao).executar(comando)); verify(movimentos,times(8)).salvar(any());
 }
 @Test void naoRepeteFalhaInesperada() {
  when(movimentos.salvar(any())).thenThrow(new IllegalStateException("falha"));
  assertThrows(IllegalStateException.class,()->service(transacao).executar(comando)); verify(movimentos).salvar(any());
 }
 @Test void consultaCatalogoEMovimentos() {
  ConsultarCatalogo consultar=new ConsultarCatalogo(catalogo);
  when(catalogo.produtos()).thenReturn(List.of(new Produto("P001","Renda fixa")));
  when(catalogo.cosifs("P001")).thenReturn(List.of(new Cosif("11111111111","000001")));
  assertEquals(1,consultar.produtos().size()); assertEquals(1,consultar.cosifs("P001").size());
  assertThrows(RecursoNaoEncontradoException.class,()->consultar.cosifs("XXXX"));
  when(movimentos.consultar(competencia)).thenReturn(List.of());
  assertTrue(new ConsultarMovimentos(movimentos).executar(competencia).isEmpty());
 }
 private IncluirMovimento service(Transacao t) { return new IncluirMovimento(movimentos,catalogo,t,clock); }
}
