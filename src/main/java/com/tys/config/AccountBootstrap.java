package com.tys.config;

import com.tys.model.Admin;
import com.tys.model.Company;
import com.tys.repository.AdminRepository;
import com.tys.repository.CompanyRepository;
import com.tys.security.PasswordService;
import com.tys.validation.InputRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Açılışta hesap güvenliği:
 * <ol>
 *   <li>Veritabanında kalmış düz metin parolalar BCrypt'e çevrilir (düz metin parola ile giriş artık kabul edilmez).</li>
 *   <li>Hiç yönetici yoksa ADMIN_USERNAME / ADMIN_INITIAL_PASSWORD ile ilk yönetici oluşturulur.
 *       Canlı profilde yönetici de bu değişkenler de yoksa uygulama açılmaz.</li>
 * </ol>
 */
@Slf4j
@Component
@Order(2)
public class AccountBootstrap implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final PasswordService passwordService;
    private final String adminUsername;
    private final String adminPassword;
    private final String profiles;

    public AccountBootstrap(AdminRepository adminRepository,
                            CompanyRepository companyRepository,
                            PasswordService passwordService,
                            @Value("${app.bootstrap.admin-username:}") String adminUsername,
                            @Value("${app.bootstrap.admin-password:}") String adminPassword,
                            @Value("${spring.profiles.active:}") String profiles) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.passwordService = passwordService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.profiles = profiles;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int migrated = 0;
        for (Admin admin : adminRepository.findAll()) {
            if (admin.getPassword() != null && !passwordService.isHashed(admin.getPassword())) {
                admin.setPassword(passwordService.hash(admin.getPassword()));
                adminRepository.save(admin);
                migrated++;
            }
        }
        for (Company company : companyRepository.findAll()) {
            if (company.getPassword() != null && !passwordService.isHashed(company.getPassword())) {
                company.setPassword(passwordService.hash(company.getPassword()));
                companyRepository.save(company);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.warn("{} hesabın düz metin parolası BCrypt'e çevrildi.", migrated);
        }

        if (adminRepository.count() > 0) {
            return;
        }
        boolean prod = profiles != null && profiles.toLowerCase().contains("prod");
        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            if (prod) {
                throw new IllegalStateException(
                        "Yönetici hesabı yok: ilk yönetici için ADMIN_USERNAME ve ADMIN_INITIAL_PASSWORD tanımlayın.");
            }
            log.warn("Yönetici hesabı yok. ADMIN_USERNAME ve ADMIN_INITIAL_PASSWORD ile oluşturulabilir.");
            return;
        }
        InputRules.username(adminUsername);
        InputRules.password(adminPassword, "ADMIN_INITIAL_PASSWORD");
        Admin admin = new Admin();
        admin.setUsername(adminUsername.trim());
        admin.setPassword(passwordService.hash(adminPassword));
        adminRepository.save(admin);
        log.info("İlk yönetici hesabı oluşturuldu: {}", admin.getUsername());
    }
}
