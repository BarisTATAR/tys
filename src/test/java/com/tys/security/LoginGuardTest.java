package com.tys.security;

import com.tys.repository.LoginLockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginGuardTest {

    private LoginGuard guard;

    @BeforeEach
    void setUp() {
        LoginLockRepository repository = mock(LoginLockRepository.class);
        when(repository.findById(anyString())).thenReturn(Optional.empty());
        // pencere 15 dk, kullanıcı+IP 5, IP 20, kullanıcı (tüm IP'ler) 50
        guard = new LoginGuard(repository, 15, 5, 20, 50);
    }

    @Test
    void besHatadanSonraAyniIpdenKullaniciKilitlenir() {
        for (int i = 0; i < 5; i++) {
            guard.recordFailure("otel_a", "203.0.113.10");
        }
        assertThatThrownBy(() -> guard.assertAllowed("otel_a", "203.0.113.10"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("429");
    }

    @Test
    void saldirganMesruKullaniciyiBaskaIpdenKilitleyemez() {
        for (int i = 0; i < 5; i++) {
            guard.recordFailure("otel_a", "203.0.113.10");
        }
        assertThatCode(() -> guard.assertAllowed("otel_a", "198.51.100.7")).doesNotThrowAnyException();
    }

    @Test
    void ipFarkliKullanicilarlaDenemeyeDevamEdinceEngellenir() {
        for (int i = 0; i < 20; i++) {
            guard.recordFailure("kullanici" + i, "203.0.113.99");
        }
        assertThatThrownBy(() -> guard.assertAllowed("baska_kullanici", "203.0.113.99"))
                .isInstanceOf(ResponseStatusException.class);
        assertThatCode(() -> guard.assertAllowed("baska_kullanici", "198.51.100.8")).doesNotThrowAnyException();
    }

    @Test
    void basariliGirisKullaniciIpSayacınıSifirlar() {
        for (int i = 0; i < 4; i++) {
            guard.recordFailure("otel_b", "203.0.113.20");
        }
        guard.recordSuccess("otel_b", "203.0.113.20");
        guard.recordFailure("otel_b", "203.0.113.20");
        assertThatCode(() -> guard.assertAllowed("otel_b", "203.0.113.20")).doesNotThrowAnyException();
    }
}
