package com.tys.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookie {

    public static final String NAME = "tys_auth";

    private final long expirationMs;
    private final String sameSite;
    private final Boolean secureOverride;

    public AuthCookie(
            @Value("${jwt.expiration-ms:1800000}") long expirationMs,
            @Value("${app.cookie.same-site:Lax}") String sameSite,
            @Value("${app.cookie.secure:}") String secureFlag) {
        this.expirationMs = expirationMs;
        String normalized = sameSite == null ? "Lax" : sameSite.trim();
        if (normalized.equalsIgnoreCase("Strict")) {
            this.sameSite = "Strict";
        } else if (normalized.equalsIgnoreCase("None")) {
            this.sameSite = "None";
        } else if (normalized.equalsIgnoreCase("Lax")) {
            this.sameSite = "Lax";
        } else {
            throw new IllegalStateException("app.cookie.same-site Lax, Strict veya None olmalıdır.");
        }
        if ("None".equals(this.sameSite)) {
            this.secureOverride = Boolean.TRUE;
        } else if (secureFlag == null || secureFlag.isBlank()) {
            this.secureOverride = null;
        } else {
            this.secureOverride = Boolean.parseBoolean(secureFlag);
        }
    }

    public void write(HttpServletRequest request, HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(request, token, Duration.ofMillis(expirationMs)).toString());
    }

    public void clear(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(request, "", Duration.ZERO).toString());
    }

    public static String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie cookie(HttpServletRequest request, String value, Duration maxAge) {
        boolean secure = secureOverride != null ? secureOverride : request.isSecure();
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();
    }
}
