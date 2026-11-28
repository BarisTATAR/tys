package com.tys.service;

import com.tys.domain.company.CompanyRoomCountRule;
import com.tys.domain.company.CompanyTssCodeUniquenessRule;
import com.tys.domain.company.CompanyUsernameUniquenessRule;
import com.tys.dto.CompanyDto;
import com.tys.mapper.CompanyMapper;
import com.tys.model.Company;
import com.tys.repository.CafeRepository;
import com.tys.repository.CompanyRepository;
import com.tys.repository.GuestRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.repository.RulesApprovalLinkRepository;
import com.tys.repository.RulesFileRepository;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.LoginRequest;
import com.tys.request.UpdateCompanyRequest;
import com.tys.response.LoginResponse;
import com.tys.security.JwtService;
import com.tys.security.PasswordService;
import com.tys.validation.InputRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final JwtService jwtService;
    private final PasswordService passwordService;
    private final CompanyUsernameUniquenessRule companyUsernameUniquenessRule;
    private final CompanyTssCodeUniquenessRule companyTssCodeUniquenessRule;
    private final CompanyRoomCountRule companyRoomCountRule;
    private final KbsCredentialService kbsCredentialService;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final CafeRepository cafeRepository;
    private final RulesFileRepository rulesFileRepository;
    private final RulesApprovalLinkRepository rulesApprovalLinkRepository;

    @Transactional
    public void createCompany(CreateCompanyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("İşletme bilgileri gereklidir.");
        }
        InputRules.username(request.getUsername());
        InputRules.password(request.getPassword(), "Şifre");
        InputRules.requiredText(request.getName(), "İşletme adı", 150);
        InputRules.facilityNumber(request.getFacilityNumber());
        InputRules.totalRoomNumber(request.getTotalRoomNumber());
        validateOptionalFields(request.getAddress(), request.getEmail(), request.getPhoneNumber(),
                request.getTaxNumber(), request.getStartYear(), request.getGoogleCommentsUrl());

        request.setUsername(request.getUsername().trim());
        companyUsernameUniquenessRule.assertAvailable(request.getUsername(), null);
        companyTssCodeUniquenessRule.assertAvailable(request.getFacilityNumber(), null);

        Company company = companyMapper.createCompanyRequestToEntity(request);
        company.setPassword(passwordService.hash(request.getPassword()));
        companyRepository.save(company);
        kbsCredentialService.save(company, request.getKbsTesisKodu(), request.getKbsKullaniciTc(), request.getKbsSifre(), true);
    }

    /**
     * Bağlı oda, rezervasyon, misafir veya kafe kaydı olan işletme silinmez (409).
     * Yalnız yapılandırma kayıtları (KBS, kurallar dosyası, onay linkleri) işletmeyle birlikte silinir.
     */
    @Transactional
    public void deleteCompany(DeleteCompanyRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Silinecek işletme seçilmelidir.");
        }
        Company company = requireCompany(request.getId());
        long rooms = roomRepository.countByCompany(company);
        long reservations = reservationRepository.countByCompany_Id(company.getId());
        long guests = guestRepository.countByCompany_Id(company.getId());
        long cafes = cafeRepository.countByCompanyId(company.getId());
        if (rooms + reservations + guests + cafes > 0) {
            List<String> parts = new ArrayList<>();
            if (rooms > 0) parts.add(rooms + " oda");
            if (reservations > 0) parts.add(reservations + " rezervasyon");
            if (guests > 0) parts.add(guests + " misafir");
            if (cafes > 0) parts.add(cafes + " kafe ürünü");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "İşletmeye bağlı kayıtlar var (" + String.join(", ", parts) + "). İşletme silinemez.");
        }
        // Ters ilişki (Company.kbs) silinen kaydı göstermesin; aksi halde flush sırasında TransientObjectException
        company.setKbs(null);
        kbsCredentialService.deleteForCompany(company.getId());
        rulesApprovalLinkRepository.deleteAllByCompanyId(company.getId());
        rulesFileRepository.findByCompanyId(company.getId()).ifPresent(rulesFileRepository::delete);
        companyRepository.delete(company);
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(Long id) {
        return toDto(requireCompany(id));
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<String> getCompanyNames() {
        return companyRepository.findAllCompanyNames();
    }

    @Transactional
    public void updateCompany(UpdateCompanyRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Güncellenecek işletme seçilmelidir.");
        }
        Company existingCompany = requireCompany(request.getId());
        if (request.getName() != null) {
            InputRules.requiredText(request.getName(), "İşletme adı", 150);
        }
        if (request.getFacilityNumber() != null) {
            InputRules.facilityNumber(request.getFacilityNumber());
        }
        if (request.getTotalRoomNumber() != null) {
            InputRules.totalRoomNumber(request.getTotalRoomNumber());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            InputRules.password(request.getPassword(), "Şifre");
        }
        // Eski kayıtlarda biçim dışı değer olabilir: yalnız değiştirilen alanlar doğrulanır
        validateOptionalFields(
                changed(request.getAddress(), existingCompany.getAddress()),
                changed(request.getEmail(), existingCompany.getEmail()),
                changed(request.getPhoneNumber(), existingCompany.getPhoneNumber()),
                changed(request.getTaxNumber(), existingCompany.getTaxNumber()),
                java.util.Objects.equals(request.getStartYear(), existingCompany.getStartYear()) ? null : request.getStartYear(),
                changed(request.getGoogleCommentsUrl(), existingCompany.getGoogleCommentsUrl()));

        companyRoomCountRule.assertTotalRoomNumberNotBelowRegistered(existingCompany, request.getTotalRoomNumber());
        companyTssCodeUniquenessRule.assertAvailable(request.getFacilityNumber(), request.getId());
        companyMapper.updateExistingCompanyWithCompanyRequest(request, existingCompany);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existingCompany.setPassword(passwordService.hash(request.getPassword()));
        }
        companyRepository.save(existingCompany);
        kbsCredentialService.save(existingCompany, request.getKbsTesisKodu(), request.getKbsKullaniciTc(), request.getKbsSifre(), false);
    }

    public LoginResponse login(LoginRequest request, Boolean isAdmin) {
        // Güvenlik için kullanıcı adı veya şifre hatalı mesajı kullan
        String errorMessage = "Kullanıcı adı veya şifre hatalı.";
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return new LoginResponse(false, errorMessage, null);
        }
        Optional<Company> optionalCompany = companyRepository.findByUsername(request.getUsername().trim());
        if (optionalCompany.isEmpty()) {
            passwordService.burnTime();
            return new LoginResponse(false, errorMessage, null);
        }
        Company company = optionalCompany.get();
        if (!passwordService.matches(request.getPassword(), company.getPassword())) {
            return new LoginResponse(false, errorMessage, null);
        }
        // JWT oluştur (companyId claim ile)
        String token = jwtService.generateToken(company.getId());
        return new LoginResponse(true, "Giriş başarılı.", token);
    }

    private CompanyDto toDto(Company company) {
        CompanyDto dto = companyMapper.toDto(company);
        var kbs = kbsCredentialService.findRecord(company.getId());
        dto.setKbsConfigured(kbsCredentialService.find(company.getId()).isPresent());
        dto.setKbsTesisKodu(kbs.map(k -> k.getCompanyCode()).orElse(null));
        return dto;
    }

    private Company requireCompany(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("İşletme seçilmelidir.");
        }
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İşletme bulunamadı."));
    }

    private static String changed(String incoming, String current) {
        if (incoming == null) {
            return null;
        }
        return incoming.trim().equals(current == null ? "" : current.trim()) ? null : incoming;
    }

    private void validateOptionalFields(String address, String email, String phone, String taxNumber,
                                        Integer startYear, String googleCommentsUrl) {
        InputRules.plainText(address, "Adres", 500, false);
        InputRules.email(email);
        InputRules.optionalMobile(phone);
        if (taxNumber != null && !taxNumber.isBlank() && !taxNumber.trim().matches("^\\d{10,11}$")) {
            throw new IllegalArgumentException("Vergi numarası 10 veya 11 haneli olmalıdır.");
        }
        InputRules.startYear(startYear);
        if (googleCommentsUrl != null && !googleCommentsUrl.isBlank()
                && !googleCommentsUrl.trim().matches("^https://[^\\s<>\"']{3,2000}$")) {
            throw new IllegalArgumentException("Google yorum bağlantısı https:// ile başlayan geçerli bir adres olmalıdır.");
        }
    }
}
