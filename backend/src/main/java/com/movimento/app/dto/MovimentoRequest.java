package com.movimento.app.dto;

import com.movimento.app.model.Competencia;
import com.movimento.app.model.NovoMovimento;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MovimentoRequest(
    @NotNull @Min(1) @Max(12) Integer mes,
    @NotNull @Min(1) @Max(9999) Integer ano,
    @NotBlank @Size(max = 4) String codigoProduto,
    @NotBlank @Size(max = 11) String codigoCosif,
    @NotBlank @Size(max = 50) String descricao,
    @NotBlank
        @Pattern(
            regexp = "-?[0-9]{1,16}(\\.[0-9]{1,2})?",
            message = "Use valor decimal com até 16 inteiros e 2 casas.")
        String valor) {
  public NovoMovimento toCommand() {
    return new NovoMovimento(
        new Competencia(mes, ano), codigoProduto, codigoCosif, descricao, new BigDecimal(valor));
  }
}
