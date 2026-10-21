package br.com.antlia.movimento.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class NovoMovimentoTest {
 @Test void preservaPrecisaoMonetaria() {
  assertEquals(new BigDecimal("9999999999999999.99"),novo("9999999999999999.99").valor());
  assertEquals(new BigDecimal("-12.30"),novo("-12.3").valor());
  assertEquals(new BigDecimal("0.00"),novo("0").valor());
 }
 @ParameterizedTest @ValueSource(strings={"1.001","10000000000000000","-10000000000000000"})
 void rejeitaValorForaDoSchema(String valor) { assertThrows(IllegalArgumentException.class,()->novo(valor)); }
 @Test void rejeitaAusenciasETamanhos() {
  Competencia c=new Competencia(1,2026);
  assertThrows(NullPointerException.class,()->new NovoMovimento(null,"P001","1","Teste",BigDecimal.ONE));
  assertThrows(IllegalArgumentException.class,()->new NovoMovimento(c,null,"1","Teste",BigDecimal.ONE));
  assertThrows(IllegalArgumentException.class,()->new NovoMovimento(c," ","1","Teste",BigDecimal.ONE));
  assertThrows(IllegalArgumentException.class,()->new NovoMovimento(c,"12345","1","Teste",BigDecimal.ONE));
  assertThrows(IllegalArgumentException.class,()->new NovoMovimento(c,"P001","123456789012","Teste",BigDecimal.ONE));
  assertThrows(IllegalArgumentException.class,()->new NovoMovimento(c,"P001","1","x".repeat(51),BigDecimal.ONE));
  assertThrows(IllegalArgumentException.class,()->new NovoMovimento(c,"P001","1","Teste",null));
  assertEquals("Teste",new NovoMovimento(c,"P001","1"," Teste ",BigDecimal.ONE).descricao());
 }
 @ParameterizedTest @CsvSource({"0,2026","13,2026","1,0","1,10000"})
 void rejeitaCompetenciaInvalida(int mes,int ano) { assertThrows(IllegalArgumentException.class,()->new Competencia(mes,ano)); }
 private NovoMovimento novo(String valor) { return new NovoMovimento(new Competencia(1,2026),"P001","11111111111","Teste",new BigDecimal(valor)); }
}
