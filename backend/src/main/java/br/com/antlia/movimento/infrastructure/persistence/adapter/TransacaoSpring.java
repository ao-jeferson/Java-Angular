package br.com.antlia.movimento.infrastructure.persistence.adapter;

import br.com.antlia.movimento.application.Transacao;
import br.com.antlia.movimento.domain.ConflitoNumeracaoException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.function.Supplier;
@Component
public class TransacaoSpring implements Transacao {
    private final TransactionTemplate template;
    public TransacaoSpring(PlatformTransactionManager manager) {
        template=new TransactionTemplate(manager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    public <T> T executar(Supplier<T> acao) {
        try { return template.execute(status -> acao.get()); }
        catch (RuntimeException ex) {
            for (Throwable cause=ex;cause!=null;cause=cause.getCause()) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                    String constraint=violation.getConstraintName();
                    if (constraint!=null && (constraint.toUpperCase().contains("UK_MOVIMENTO_COMPETENCIA") || constraint.toUpperCase().contains("PRIMARY_KEY")))
                        throw new ConflitoNumeracaoException();
                }
            }
            throw ex;
        }
    }
}
