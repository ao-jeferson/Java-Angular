package br.com.antlia.movimento.infrastructure.security;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.*;
import java.time.*;
import java.util.List;
@Service
public class AutenticacaoService {
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String passwordHash;
    private final JwtEncoder encoder;
    private final String issuer;
    private final String audience;
    private final Duration ttl;
    private final Clock clock;
    public AutenticacaoService(PasswordEncoder passwordEncoder,JwtEncoder encoder,Clock clock,
        @Value("${movimento.auth.username}") String username,@Value("${movimento.auth.password}") String password,
        @Value("${movimento.auth.issuer}") String issuer,@Value("${movimento.auth.audience}") String audience,
        @Value("${movimento.auth.ttl}") Duration ttl) {
        if(password.length()<12 || password.length()>72) throw new IllegalArgumentException("Configure MOVIMENTO_PASSWORD com 12 a 72 caracteres.");
        this.passwordEncoder=passwordEncoder; this.username=username; this.passwordHash=passwordEncoder.encode(password);
        this.encoder=encoder; this.issuer=issuer; this.audience=audience; this.ttl=ttl; this.clock=clock;
    }
    public Token autenticar(String user,String password) {
        boolean matches=passwordEncoder.matches(password,passwordHash);
        if(!username.equals(user) || !matches) throw new BadCredentialsException("Usuário ou senha inválidos.");
        Instant now=clock.instant();
        JwtClaimsSet claims=JwtClaimsSet.builder().issuer(issuer).subject(username).audience(List.of(audience))
            .issuedAt(now).expiresAt(now.plus(ttl)).claim("scope","movimentos.read movimentos.write").build();
        return new Token(encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(),ttl.toSeconds(),"Bearer");
    }
    public record Token(String accessToken,long expiresIn,String tokenType) {}
}
