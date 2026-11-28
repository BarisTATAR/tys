package com.tys.service;

import com.tys.model.Admin;
import com.tys.repository.AdminRepository;
import com.tys.request.LoginRequest;
import com.tys.response.LoginResponse;
import com.tys.security.JwtService;
import com.tys.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final String LOGIN_ERROR = "Kullanıcı adı veya şifre hatalı.";

    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return new LoginResponse(false, LOGIN_ERROR, null);
        }
        Optional<Admin> optionalAdmin = adminRepository.findByUsername(request.getUsername().trim());
        if (optionalAdmin.isEmpty()) {
            passwordService.burnTime();
            return new LoginResponse(false, LOGIN_ERROR, null);
        }
        Admin admin = optionalAdmin.get();
        if (!passwordService.matches(request.getPassword(), admin.getPassword())) {
            return new LoginResponse(false, LOGIN_ERROR, null);
        }
        String token = jwtService.generateAdminToken(admin.getId());
        return new LoginResponse(true, "Giriş başarılı.", token);
    }
}
