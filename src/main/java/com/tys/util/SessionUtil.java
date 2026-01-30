package com.tys.util;

import com.tys.service.TokenService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionUtil {

    private static final String SESSION_TOKEN_KEY = "COMPANY_TOKEN";
    private static final String SESSION_COMPANY_ID_KEY = "COMPANY_ID";
    
    private final TokenService tokenService;

    /**
     * Session'dan token'ı alır
     */
    public String getTokenFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(SESSION_TOKEN_KEY);
    }

    /**
     * Session'dan companyId'yi alır
     * Önce direkt session'dan almayı dener, yoksa token'dan çıkarır
     */
    public Long getCompanyIdFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        
        // Önce direkt session'dan company ID'yi almayı dene
        Object companyIdObj = session.getAttribute(SESSION_COMPANY_ID_KEY);
        if (companyIdObj instanceof Long) {
            return (Long) companyIdObj;
        }
        
        // Yoksa token'dan çıkar
        String token = getTokenFromSession(session);
        if (token != null && tokenService != null) {
            Long companyId = tokenService.extractCompanyId(token);
            // Bulduysa session'a kaydet
            if (companyId != null) {
                session.setAttribute(SESSION_COMPANY_ID_KEY, companyId);
            }
            return companyId;
        }
        return null;
    }

    /**
     * Session'ın geçerli olup olmadığını kontrol eder
     */
    public boolean isSessionValid(HttpSession session) {
        if (session == null) {
            return false;
        }
        
        try {
            // Session'ın geçerli olup olmadığını kontrol et
            session.getAttribute(SESSION_TOKEN_KEY);
            
            String token = getTokenFromSession(session);
            if (token == null || token.isEmpty()) {
                return false;
            }
            
            Long companyId = tokenService.extractCompanyId(token);
            return companyId != null;
        } catch (IllegalStateException e) {
            // Session invalid veya expired
            return false;
        } catch (Exception e) {
            // Diğer hatalar
            return false;
        }
    }

    /**
     * Session'a token kaydeder
     */
    public void setTokenToSession(HttpSession session, String token) {
        if (session != null) {
            session.setAttribute(SESSION_TOKEN_KEY, token);
            // Token'dan company ID'yi çıkar ve session'a kaydet
            Long companyId = tokenService.extractCompanyId(token);
            if (companyId != null) {
                session.setAttribute(SESSION_COMPANY_ID_KEY, companyId);
            }
            // Session'ı yenile (timeout süresini sıfırla)
            session.setMaxInactiveInterval(7200); // 2 saat (saniye cinsinden)
        }
    }
    
    /**
     * Session'a company ID kaydeder
     */
    public void setCompanyIdToSession(HttpSession session, Long companyId) {
        if (session != null && companyId != null) {
            session.setAttribute(SESSION_COMPANY_ID_KEY, companyId);
            // Session'ı yenile (timeout süresini sıfırla)
            session.setMaxInactiveInterval(7200); // 2 saat (saniye cinsinden)
        }
    }
    
    /**
     * Session'ı yeniler (timeout süresini sıfırlar)
     */
    public void touchSession(HttpSession session) {
        if (session != null) {
            try {
                // Session'ı dokunarak yenile
                session.setAttribute(SESSION_TOKEN_KEY, session.getAttribute(SESSION_TOKEN_KEY));
            } catch (IllegalStateException e) {
                // Session zaten invalid
            }
        }
    }

    /**
     * Session'ı temizler (logout)
     */
    public void clearSession(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_TOKEN_KEY);
            session.removeAttribute(SESSION_COMPANY_ID_KEY);
            session.invalidate();
        }
    }
}
