package br.com.antlia.movimento.infrastructure.configuration;

import br.com.antlia.movimento.domain.*;
import br.com.antlia.movimento.application.*;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.time.*;
@Configuration
public class UseCaseConfiguration {
    @Bean Clock clock(@Value("${movimento.zone}") String zone) { return Clock.system(ZoneId.of(zone)); }
    @Bean IncluirMovimento incluir(MovimentoRepository m,CatalogoRepository c,Transacao t,Clock clock) { return new IncluirMovimento(m,c,t,clock); }
    @Bean ConsultarMovimentos consultar(MovimentoRepository m) { return new ConsultarMovimentos(m); }
    @Bean ConsultarCatalogo catalogo(CatalogoRepository c) { return new ConsultarCatalogo(c); }
}
