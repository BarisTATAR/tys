package com.tys.config;

import com.tys.security.JwtAuthenticationEntryPoint;
import com.tys.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000,http://localhost,http://127.0.0.1}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight: tüm OPTIONS (AntPathRequestMatcher ile PathPattern hatası önlenir)
                        .requestMatchers(new AntPathRequestMatcher("/**", "OPTIONS")).permitAll()
                        // Login ve kayıt: token gerekmez
                        .requestMatchers("/company/login", "/company/login/").permitAll()
                        .requestMatchers("/company/admin_login", "/company/admin_login/").permitAll()
                        .requestMatchers("/admin/login", "/admin/login/").permitAll()
                        // Misafir kurallar sayfası kimliksiz açılır. İşletme kaydı yalnızca yönetici oturumundadır.
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/company/create", "/company/create/",
                                "/company/delete", "/company/delete/",
                                "/company/update", "/company/update/",
                                "/company/all",
                                "/company/names"
                        ).hasRole("ADMIN")
                        // Aşağıdaki tüm path'ler JWT gerektirir (Ant matcher kullanımı)
                        .requestMatchers(new AntPathRequestMatcher("/company/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/cafe/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/room/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/payment/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/guest/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/reservation/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/reservation-cafe-items/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/kbs/**")).authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"Bu işlem için yetkiniz yok.\",\"error\":\"FORBIDDEN\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
