package br.com.antlia.movimento.application;

import br.com.antlia.movimento.domain.*;
import java.time.Clock;
import java.time.LocalDateTime;
public final class IncluirMovimento {
    private final MovimentoRepository movimentos;
    private final CatalogoRepository catalogo;
    private final Transacao transacao;
    private final Clock clock;
    public IncluirMovimento(MovimentoRepository movimentos, CatalogoRepository catalogo, Transacao transacao, Clock clock) {
        this.movimentos = movimentos; this.catalogo = catalogo; this.transacao = transacao; this.clock = clock;
    }
    public Movimento executar(NovoMovimento comando) {
        for (int tentativa = 0; tentativa < 8; tentativa++) {
            try { return transacao.executar(() -> incluir(comando)); }
            catch (ConflitoNumeracaoException ex) { if (tentativa == 7) throw ex; }
        }
        throw new IllegalStateException("Tentativas esgotadas.");
    }
    private Movimento incluir(NovoMovimento comando) {
        Produto produto = catalogo.produto(comando.codigoProduto())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
        if (!catalogo.possuiCosif(produto.codigo(), comando.codigoCosif()))
            throw new RegraNegocioException("Cosif não pertence ao produto informado.");
        long numero = movimentos.ultimoNumero(comando.competencia()) + 1;
        if (numero > 999999999999999999L) throw new RegraNegocioException("Limite de lançamentos da competência atingido.");
        return movimentos.salvar(new Movimento(comando.competencia(), numero, produto.codigo(),
            comando.codigoCosif(), produto.descricao(), comando.descricao(), comando.valor(),
            LocalDateTime.now(clock), "TESTE"));
    }
}
