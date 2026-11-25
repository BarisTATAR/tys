package com.tys.service;

import com.tys.model.Company;
import com.tys.model.Kbs;
import com.tys.repository.KbsRepository;
import com.tys.security.SecretCipher;
import com.tys.validation.InputRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * İşletme başına KBS bilgileri. Şifre AES-GCM ile şifreli saklanır, API yanıtlarında asla dönmez.
 */
@Service
@RequiredArgsConstructor
public class KbsCredentialService {

    public static final String NOT_CONFIGURED = "İşletmenin KBS tesis bilgileri tanımlı değil. Yönetici panelinden işletme KBS bilgilerini girin.";

    private final KbsRepository kbsRepository;
    private final SecretCipher secretCipher;

    @Transactional(readOnly = true)
    public Optional<KbsCredentials> find(Long companyId) {
        if (companyId == null) {
            return Optional.empty();
        }
        return kbsRepository.findByCompany_Id(companyId)
                .filter(this::complete)
                .map(kbs -> new KbsCredentials(
                        Long.parseLong(kbs.getCompanyCode().trim()),
                        Long.parseLong(kbs.getIdentityNumber().trim()),
                        secretCipher.decrypt(kbs.getPassword())));
    }

    public KbsCredentials require(Long companyId) {
        return find(companyId).orElseThrow(() -> new IllegalStateException(NOT_CONFIGURED));
    }

    @Transactional(readOnly = true)
    public Optional<Kbs> findRecord(Long companyId) {
        return companyId == null ? Optional.empty() : kbsRepository.findByCompany_Id(companyId);
    }

    /**
     * Oluşturma: üç alan birlikte verilmeli. Güncelleme: boş alanlar korunur, şifre yalnız doluysa değişir.
     * Hiçbir alan verilmediyse işlem yapılmaz.
     */
    @Transactional
    public void save(Company company, String tesisKodu, String kullaniciTc, String sifre, boolean creating) {
        boolean anyGiven = hasText(tesisKodu) || hasText(kullaniciTc) || hasText(sifre);
        if (!anyGiven) {
            return;
        }
        Kbs kbs = kbsRepository.findByCompany_Id(company.getId()).orElseGet(Kbs::new);
        boolean isNew = kbs.getId() == null;
        if (creating || isNew) {
            if (!hasText(tesisKodu) || !hasText(kullaniciTc) || !hasText(sifre)) {
                throw new IllegalArgumentException("KBS için tesis kodu, kullanıcı TCKN ve şifre birlikte girilmelidir.");
            }
        }
        if (hasText(tesisKodu)) {
            kbs.setCompanyCode(String.valueOf(InputRules.kbsNumber(tesisKodu, "KBS tesis kodu")));
        }
        if (hasText(kullaniciTc)) {
            InputRules.requireTckn(kullaniciTc.trim());
            kbs.setIdentityNumber(kullaniciTc.trim());
        }
        if (hasText(sifre)) {
            if (sifre.length() > 100) {
                throw new IllegalArgumentException("KBS şifresi en fazla 100 karakter olabilir.");
            }
            kbs.setPassword(secretCipher.encrypt(sifre));
        }
        kbs.setCompany(company);
        kbsRepository.save(kbs);
    }

    @Transactional
    public void deleteForCompany(Long companyId) {
        kbsRepository.findByCompany_Id(companyId).ifPresent(kbsRepository::delete);
    }

    private boolean complete(Kbs kbs) {
        return hasText(kbs.getCompanyCode()) && hasText(kbs.getIdentityNumber())
                && hasText(kbs.getPassword()) && secretCipher.isEncrypted(kbs.getPassword());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
