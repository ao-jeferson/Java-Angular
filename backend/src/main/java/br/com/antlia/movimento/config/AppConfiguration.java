package br.com.antlia.movimento.config;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class AppConfiguration {
  @Bean
  Clock clock(@Value("${movimento.zone}") String zone) {
    return Clock.system(ZoneId.of(zone));
  }
}
