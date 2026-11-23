package com.tys.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentActor {

    public boolean isAdmin() {
        Authentication auth = current();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public Long requireCompanyId() {
        Authentication auth = current();
        if (auth == null || !(auth.getPrincipal() instanceof Long companyId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kimlik doğrulanamadı.");
        }
        if (isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu işlem işletme oturumu gerektirir.");
        }
        return companyId;
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu işlem yönetici oturumu gerektirir.");
        }
    }

    private Authentication current() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
