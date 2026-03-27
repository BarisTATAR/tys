package com.tys.config;

import com.tys.security.JwtAuthenticationEntryPoint;
import com.tys.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight: tüm OPTIONS (AntPathRequestMatcher ile PathPattern hatası önlenir)
                        .requestMatchers(new AntPathRequestMatcher("/**", "OPTIONS")).permitAll()
                        // Login ve kayıt: token gerekmez
                        .requestMatchers("/company/login", "/company/login/").permitAll()
                        .requestMatchers("/company/admin_login", "/company/admin_login/").permitAll()
                        .requestMatchers("/admin/login", "/admin/login/").permitAll()
                        .requestMatchers("/company/create", "/company/create/").permitAll()
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000", "http://127.0.0.1:3000",
                "http://localhost", "http://127.0.0.1",
                "http://localhost:80", "http://127.0.0.1:80"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
