package br.com.antlia.movimento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class AutenticacaoServiceTest {
  private final PasswordEncoder passwords = mock(PasswordEncoder.class);
  private final JwtEncoder encoder = mock(JwtEncoder.class);

  @Test
  void rejeitaSenhaDeConfiguracaoCurtaOuLongaDemais() {
    assertThrows(IllegalArgumentException.class, () -> service("curta"));
    assertThrows(IllegalArgumentException.class, () -> service("x".repeat(73)));
  }

  @Test
  void rejeitaUsuarioInexistenteMesmoComSenhaCorreta() {
    when(passwords.encode(anyString())).thenReturn("hash");
    when(passwords.matches("senha-correta", "hash")).thenReturn(true);
    var auth = service("senha-correta");
    assertThrows(BadCredentialsException.class, () -> auth.autenticar("outro", "senha-correta"));
    verify(passwords).matches("senha-correta", "hash");
    verifyNoInteractions(encoder);
  }

  private AutenticacaoService service(String password) {
    return new AutenticacaoService(
        passwords,
        encoder,
        Clock.systemUTC(),
        "admin",
        password,
        "movimento",
        "movimento-api",
        Duration.ofMinutes(15));
  }
}
