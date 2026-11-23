package com.tys.service;

import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
public class TokenService {

    private static final String SEPARATOR = ":";

    /**
     * CompanyId ile token oluşturur
     * Format: base64(companyId:UUID)
     */
    public String generateToken(Long companyId) {
        String uuid = UUID.randomUUID().toString();
        String tokenData = companyId + SEPARATOR + uuid;
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    /**
     * Token'dan companyId'yi çıkarır
     */
    public Long extractCompanyId(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(SEPARATOR);
            if (parts.length >= 1) {
                return Long.parseLong(parts[0]);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Token'ın geçerli olup olmadığını kontrol eder
     */
    public boolean isValidToken(String token) {
        return extractCompanyId(token) != null;
    }
}



