package com.tys.security;

import com.tys.model.LoginLock;
import com.tys.repository.LoginLockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class LoginGuard {

    private static final int MAX_FAILURES = 5;
    private static final ZoneId ZONE = ZoneId.of("Europe/Istanbul");
    private final LoginLockRepository loginLockRepository;

    public LoginGuard(LoginLockRepository loginLockRepository) {
        this.loginLockRepository = loginLockRepository;
    }

    @Transactional(readOnly = true)
    public void assertAllowed(String username) {
        loginLockRepository.findById(key(username)).ifPresent(lock -> {
            if (lock.getLockedUntil() != null && LocalDateTime.now(ZONE).isBefore(lock.getLockedUntil())) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Çok fazla hatalı deneme. 15 dakika sonra tekrar deneyin.");
            }
        });
    }

    @Transactional
    public void recordFailure(String username) {
        String id = key(username);
        LocalDateTime now = LocalDateTime.now(ZONE);
        LoginLock lock = loginLockRepository.findById(id).orElseGet(LoginLock::new);
        if (lock.getUsername() == null) {
            lock.setUsername(id);
            lock.setFailures(0);
        }
        if (lock.getLockedUntil() != null && now.isAfter(lock.getLockedUntil())) {
            lock.setFailures(0);
            lock.setLockedUntil(null);
        }
        lock.setFailures(lock.getFailures() + 1);
        if (lock.getFailures() >= MAX_FAILURES) {
            lock.setLockedUntil(now.plusMinutes(15));
        }
        loginLockRepository.save(lock);
    }

    @Transactional
    public void recordSuccess(String username) {
        loginLockRepository.deleteById(key(username));
    }

    private String key(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
