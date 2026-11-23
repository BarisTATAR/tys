package com.tys.domain.company;

import com.tys.exception.DuplicateCompanyUsernameException;
import com.tys.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyUsernameUniquenessRule {

    private final CompanyRepository companyRepository;

    public void assertAvailable(String username, Long excludeCompanyId) {
        if (username == null || username.isBlank()) {
            return;
        }
        String normalized = username.trim();
        boolean exists = excludeCompanyId == null
                ? companyRepository.existsByUsernameIgnoreCase(normalized)
                : companyRepository.existsByUsernameIgnoreCaseAndIdNot(normalized, excludeCompanyId);
        if (exists) {
            throw new DuplicateCompanyUsernameException();
        }
    }
}
