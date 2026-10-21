package br.com.antlia.movimento.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Movimento(
    Competencia competencia,
    long numeroLancamento,
    String codigoProduto,
    String codigoCosif,
    String descricaoProduto,
    String descricao,
    BigDecimal valor,
    LocalDateTime dataMovimento,
    String codigoUsuario) {}
