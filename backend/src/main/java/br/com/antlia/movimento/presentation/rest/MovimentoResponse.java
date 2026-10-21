package br.com.antlia.movimento.presentation.rest;

import br.com.antlia.movimento.domain.Movimento;
import java.time.LocalDateTime;
public record MovimentoResponse(int mes,int ano,String numeroLancamento,String codigoProduto,String codigoCosif,
        String descricaoProduto,String descricao,String valor,LocalDateTime dataMovimento,String codigoUsuario) {
    public static MovimentoResponse from(Movimento m) {
        return new MovimentoResponse(m.competencia().mes(),m.competencia().ano(),Long.toString(m.numeroLancamento()),m.codigoProduto(),m.codigoCosif(),m.descricaoProduto(),m.descricao(),m.valor().toPlainString(),m.dataMovimento(),m.codigoUsuario());
    }
}
