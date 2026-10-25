package com.tys.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String CLAIM_COMPANY_ID = "companyId";
    private static final String CLAIM_ADMIN_ID = "adminId";
    private static final String CLAIM_ROLE = "role";
    public static final String ROLE_COMPANY = "COMPANY";
    public static final String ROLE_ADMIN = "ADMIN";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Şirket girişi için JWT (company/login, company/admin_login) */
    public String generateToken(Long companyId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(companyId))
                .claim(CLAIM_COMPANY_ID, companyId)
                .claim(CLAIM_ROLE, ROLE_COMPANY)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /** Admin girişi için JWT (admin/login) */
    public String generateAdminToken(Long adminId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim(CLAIM_ADMIN_ID, adminId)
                .claim(CLAIM_ROLE, ROLE_ADMIN)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getCompanyIdFromToken(String token) {
        if (!ROLE_COMPANY.equals(getRoleFromToken(token))) return null;
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object companyId = claims.get(CLAIM_COMPANY_ID);
        if (companyId instanceof Number) {
            return ((Number) companyId).longValue();
        }
        return null;
    }

    public Long getAdminIdFromToken(String token) {
        if (!ROLE_ADMIN.equals(getRoleFromToken(token))) return null;
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object adminId = claims.get(CLAIM_ADMIN_ID);
        if (adminId instanceof Number) {
            return ((Number) adminId).longValue();
        }
        return null;
    }

    /** Principal id: company token'da companyId, admin token'da adminId */
    public Long getPrincipalIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        String role = getRoleFromToken(token);
        if (ROLE_ADMIN.equals(role)) {
            Object adminId = claims.get(CLAIM_ADMIN_ID);
            return adminId instanceof Number ? ((Number) adminId).longValue() : null;
        }
        Object companyId = claims.get(CLAIM_COMPANY_ID);
        return companyId instanceof Number ? ((Number) companyId).longValue() : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object role = claims.get(CLAIM_ROLE);
        return role != null ? role.toString() : ROLE_COMPANY;
    }

    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
