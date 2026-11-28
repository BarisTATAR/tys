package com.tys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

// Kimlik doğrulama JWT filtresiyle yapılır; Spring'in rastgele parolalı varsayılan "user" hesabı oluşturulmaz (loga parola yazılmaz).
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableAsync
public class TysApplication {

    /** İşletme günleri (ödeme tarihi, giriş/çıkış, raporlar) sunucunun saat diliminden bağımsız olarak İstanbul saatine göredir. */
    static final String BUSINESS_TIME_ZONE = "Europe/Istanbul";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(System.getProperty("app.timezone", BUSINESS_TIME_ZONE)));
        SpringApplication.run(TysApplication.class, args);
    }
}
