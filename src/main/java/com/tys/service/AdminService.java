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

    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    public LoginResponse login(LoginRequest request) {
        Optional<Admin> optionalAdmin = adminRepository.findByUsername(request.getUsername());

        if (optionalAdmin.isEmpty()) {
            return new LoginResponse(false, "Kullanıcı adı veya şifre hatalı.", null);
        }

        Admin admin = optionalAdmin.get();

        if (!passwordService.matches(request.getPassword(), admin.getPassword())) {
            return new LoginResponse(false, "Kullanıcı adı veya şifre hatalı.", null);
        }
        if (!passwordService.isHashed(admin.getPassword())) {
            admin.setPassword(passwordService.hash(request.getPassword()));
            adminRepository.save(admin);
        }

        String token = jwtService.generateAdminToken(admin.getId());
        return new LoginResponse(true, "Giriş başarılı.", token);
    }
}
