package br.com.antlia.movimento.infrastructure.security;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;
@Configuration
public class SecurityConfiguration {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean RSAKey signingKey() throws GeneralSecurityException {
        KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA"); generator.initialize(2048);
        KeyPair pair=generator.generateKeyPair();
        return new RSAKey.Builder((RSAPublicKey)pair.getPublic()).privateKey((RSAPrivateKey)pair.getPrivate()).keyID(UUID.randomUUID().toString()).build();
    }
    @Bean JwtEncoder jwtEncoder(RSAKey key) { return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key))); }
    @Bean JwtDecoder jwtDecoder(RSAKey key,@Value("${movimento.auth.issuer}") String issuer,@Value("${movimento.auth.audience}") String audience) throws com.nimbusds.jose.JOSEException {
        NimbusJwtDecoder decoder=NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build();
        OAuth2TokenValidator<Jwt> audienceValidator=jwt -> jwt.getAudience().contains(audience) ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Audience inválida",null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer),audienceValidator));
        return decoder;
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.cors(cors -> {}).csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login","/actuator/health","/error","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/v1/**").hasAuthority("SCOPE_movimentos.read")
                .requestMatchers(org.springframework.http.HttpMethod.POST,"/api/v1/movimentos").hasAuthority("SCOPE_movimentos.write")
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {})
                .authenticationEntryPoint((req,res,ex) -> error(res,401,"Autenticação necessária ou token inválido."))
                .accessDeniedHandler((req,res,ex) -> error(res,403,"Acesso não permitido.")))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req,res,e) -> error(res,401,"Autenticação necessária."))
                .accessDeniedHandler((req,res,e) -> error(res,403,"Acesso não permitido.")))
            .build();
    }
    private static void error(jakarta.servlet.http.HttpServletResponse response,int status,String detail) throws java.io.IOException {
        response.setStatus(status); response.setContentType("application/problem+json"); response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Erro de autenticação\",\"status\":"+status+",\"detail\":\""+detail+"\"}");
    }
    @Bean CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config=new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200","http://127.0.0.1:4200"));
        config.setAllowedMethods(List.of("GET","POST","OPTIONS")); config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/api/**",config); return source;
    }
}
