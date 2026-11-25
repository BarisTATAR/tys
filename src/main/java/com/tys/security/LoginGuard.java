package com.tys.security;

import com.tys.model.LoginLock;
import com.tys.repository.LoginLockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kaba kuvvet koruması (üç katman):
 * <ul>
 *   <li>kullanıcı + IP: {@code user-ip-max-failures} hatalı denemede o IP'den o kullanıcıya kilit
 *       (saldırgan, meşru kullanıcının kendi IP'sinden girişini kilitleyemez);</li>
 *   <li>IP: {@code ip-max-failures} hatalı denemede IP'nin tüm giriş denemeleri engellenir;</li>
 *   <li>kullanıcı (DB, tüm IP'ler): {@code user-max-failures} hatalı denemede dağıtık saldırıya karşı kilit.</li>
 * </ul>
 * İstemci IP'si {@code server.forward-headers-strategy=native} ile yalnız güvenilen vekillerden alınır.
 */
@Component
public class LoginGuard {

    private static final ZoneId ZONE = ZoneId.of("Europe/Istanbul");
    static final String LOCKED_MESSAGE = "Çok fazla hatalı deneme. 15 dakika sonra tekrar deneyin.";

    private final LoginLockRepository loginLockRepository;
    private final Duration window;
    private final int userIpMaxFailures;
    private final int ipMaxFailures;
    private final int userMaxFailures;
    private final Map<String, Counter> userIpCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> ipCounters = new ConcurrentHashMap<>();

    public LoginGuard(LoginLockRepository loginLockRepository,
                      @Value("${app.security.login.window-minutes:15}") int windowMinutes,
                      @Value("${app.security.login.user-ip-max-failures:5}") int userIpMaxFailures,
                      @Value("${app.security.login.ip-max-failures:20}") int ipMaxFailures,
                      @Value("${app.security.login.user-max-failures:50}") int userMaxFailures) {
        this.loginLockRepository = loginLockRepository;
        this.window = Duration.ofMinutes(Math.max(1, windowMinutes));
        this.userIpMaxFailures = Math.max(1, userIpMaxFailures);
        this.ipMaxFailures = Math.max(1, ipMaxFailures);
        this.userMaxFailures = Math.max(1, userMaxFailures);
    }

    @Transactional(readOnly = true)
    public void assertAllowed(String username, String clientIp) {
        Instant now = Instant.now();
        if (blocked(ipCounters.get(ipKey(clientIp)), now) || blocked(userIpCounters.get(userIpKey(username, clientIp)), now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, LOCKED_MESSAGE);
        }
        loginLockRepository.findById(key(username)).ifPresent(lock -> {
            if (lock.getLockedUntil() != null && LocalDateTime.now(ZONE).isBefore(lock.getLockedUntil())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, LOCKED_MESSAGE);
            }
        });
    }

    @Transactional
    public void recordFailure(String username, String clientIp) {
        Instant now = Instant.now();
        purgeIfLarge(now);
        increment(ipCounters, ipKey(clientIp), ipMaxFailures, now);
        increment(userIpCounters, userIpKey(username, clientIp), userIpMaxFailures, now);

        String id = key(username);
        LocalDateTime localNow = LocalDateTime.now(ZONE);
        LoginLock lock = loginLockRepository.findById(id).orElseGet(LoginLock::new);
        if (lock.getUsername() == null) {
            lock.setUsername(id);
            lock.setFailures(0);
        }
        if (lock.getLockedUntil() != null && localNow.isAfter(lock.getLockedUntil())) {
            lock.setFailures(0);
            lock.setLockedUntil(null);
        }
        lock.setFailures(lock.getFailures() + 1);
        if (lock.getFailures() >= userMaxFailures) {
            lock.setLockedUntil(localNow.plus(window));
        }
        loginLockRepository.save(lock);
    }

    @Transactional
    public void recordSuccess(String username, String clientIp) {
        userIpCounters.remove(userIpKey(username, clientIp));
        loginLockRepository.deleteById(key(username));
    }

    private static boolean blocked(Counter counter, Instant now) {
        return counter != null && counter.lockedUntil != null && now.isBefore(counter.lockedUntil);
    }

    private void increment(Map<String, Counter> map, String key, int max, Instant now) {
        map.compute(key, (k, existing) -> {
            Counter counter = existing;
            if (counter == null || now.isAfter(counter.windowStart.plus(window))
                    || (counter.lockedUntil != null && now.isAfter(counter.lockedUntil))) {
                counter = new Counter(now);
            }
            counter.failures++;
            if (counter.failures >= max) {
                counter.lockedUntil = now.plus(window);
            }
            return counter;
        });
    }

    private void purgeIfLarge(Instant now) {
        if (ipCounters.size() + userIpCounters.size() < 20_000) {
            return;
        }
        ipCounters.values().removeIf(c -> expired(c, now));
        userIpCounters.values().removeIf(c -> expired(c, now));
    }

    private boolean expired(Counter c, Instant now) {
        Instant end = c.lockedUntil != null ? c.lockedUntil : c.windowStart.plus(window);
        return now.isAfter(end);
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private static String ipKey(String ip) {
        return ip == null ? "-" : ip.trim();
    }

    private static String userIpKey(String username, String ip) {
        return key(username) + "|" + ipKey(ip);
    }

    private static final class Counter {
        private final Instant windowStart;
        private int failures;
        private Instant lockedUntil;

        private Counter(Instant windowStart) {
            this.windowStart = windowStart;
        }
    }
}
