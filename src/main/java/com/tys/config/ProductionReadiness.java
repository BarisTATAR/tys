package com.tys.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class ProductionReadiness implements ApplicationRunner {

    @Value("${spring.profiles.active:}")
    private String profiles;

    @Value("${spring.datasource.password:}")
    private String databasePassword;

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    @Value("${sms.api-id:}")
    private String smsApiId;

    @Value("${sms.api-key:}")
    private String smsApiKey;

    /** İşletme KBS şifrelerini çözmek için; canlıda JWT anahtarından türetilmez, ayrı verilmelidir. */
    @Value("${app.encryption-key:}")
    private String encryptionKey;

    @Value("${app.frontend-base-url:}")
    private String frontendBaseUrl;

    @Override
    public void run(ApplicationArguments args) {
        if (profiles == null || !profiles.toLowerCase().contains("prod")) {
            return;
        }
        List<String> missing = new ArrayList<>();
        if (databasePassword.isBlank()) {
            missing.add("SPRING_DATASOURCE_PASSWORD");
        }
        if (isLocalhostOnly(allowedOrigins)) {
            missing.add("CORS_ALLOWED_ORIGINS (canlı site adresi)");
        }
        if (smsApiId.isBlank() || smsApiKey.isBlank()) {
            missing.add("SMS_API_ID ve SMS_API_KEY");
        }
        if (encryptionKey == null || encryptionKey.isBlank()) {
            missing.add("APP_ENCRYPTION_KEY (32 bayt Base64; işletme KBS şifreleri için)");
        }
        if (frontendBaseUrl == null || !frontendBaseUrl.trim().toLowerCase().startsWith("https://")) {
            missing.add("FRONTEND_BASE_URL (https:// ile canlı site adresi; SMS onay linkleri için)");
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Canlı profil eksik ayarla başlamadı: " + String.join(", ", missing) + ".");
        }
    }

    private boolean isLocalhostOnly(String origins) {
        if (origins == null || origins.isBlank()) {
            return true;
        }
        for (String origin : origins.split(",")) {
            String value = origin.trim().toLowerCase();
            if (value.isEmpty()) {
                continue;
            }
            if (!value.contains("localhost") && !value.contains("127.0.0.1")) {
                return false;
            }
        }
        return true;
    }
}
