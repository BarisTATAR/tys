package com.tys.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    /** Login ve CORS preflight (OPTIONS) isteklerinde JWT filtresini çalıştırma - path veya sonu (Docker/context path uyumlu) */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        if (path == null) return false;
        String n = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return n.endsWith("/company/login") || n.equals("/company/login")
                || n.endsWith("/company/admin_login") || n.equals("/company/admin_login")
                || n.endsWith("/admin/login") || n.equals("/admin/login");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        if (!jwtService.validateToken(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long principalId = jwtService.getPrincipalIdFromToken(token);
        String role = jwtService.getRoleFromToken(token);
        if (principalId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authority = "ROLE_" + role;
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principalId, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
