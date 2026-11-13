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

    @Value("${kbs.tesis-kodu:0}")
    private String kbsTesisKodu;

    @Value("${kbs.kullanici-tc:0}")
    private String kbsKullaniciTc;

    @Value("${kbs.sifre:}")
    private String kbsSifre;

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
        if (kbsTesisKodu.isBlank() || "0".equals(kbsTesisKodu.trim())
                || kbsKullaniciTc.isBlank() || "0".equals(kbsKullaniciTc.trim())
                || kbsSifre.isBlank()) {
            missing.add("KBS_TESIS_KODU, KBS_KULLANICI_TC ve KBS_SIFRE");
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
