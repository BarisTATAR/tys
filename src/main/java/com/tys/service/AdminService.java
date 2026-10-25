package com.tys.service;

import com.tys.model.Admin;
import com.tys.repository.AdminRepository;
import com.tys.request.LoginRequest;
import com.tys.response.LoginResponse;
import com.tys.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Optional<Admin> optionalAdmin = adminRepository.findByUsername(request.getUsername());

        if (optionalAdmin.isEmpty()) {
            return new LoginResponse(false, "Kullanıcı bulunamadı.", null);
        }

        Admin admin = optionalAdmin.get();

        if (!admin.getPassword().equals(request.getPassword())) {
            return new LoginResponse(false, "Şifre hatalı.", null);
        }

        String token = jwtService.generateAdminToken(admin.getId());
        return new LoginResponse(true, "Giriş başarılı.", token);
    }
}
