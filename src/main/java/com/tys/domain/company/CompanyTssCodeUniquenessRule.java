package com.tys.domain.company;

import com.tys.exception.DuplicateCompanyTssCodeException;
import com.tys.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TSS kodu, işletme kaydındaki {@code facilityNumber} alanı ile temsil edilir.
 */
@Component
@RequiredArgsConstructor
public class CompanyTssCodeUniquenessRule {

    private final CompanyRepository companyRepository;

    public void assertAvailable(String tssCode, Long excludeCompanyId) {
        if (tssCode == null || tssCode.isBlank()) {
            return;
        }
        String normalized = tssCode.trim();
        boolean exists = excludeCompanyId == null
                ? companyRepository.existsByFacilityNumberIgnoreCase(normalized)
                : companyRepository.existsByFacilityNumberIgnoreCaseAndIdNot(normalized, excludeCompanyId);
        if (exists) {
            throw new DuplicateCompanyTssCodeException();
        }
    }
}
