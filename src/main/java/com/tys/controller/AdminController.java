package com.tys.controller;

import com.tys.request.LoginRequest;
import com.tys.response.LoginResponse;
import com.tys.service.AdminService;
import com.tys.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {
    private final AdminService adminService;
    private final SessionUtil sessionUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = adminService.login(request);

        if (response.isSuccess() && response.getToken() != null) {
            // Session'a token kaydet
            sessionUtil.setTokenToSession(session, response.getToken());
        }

        return ResponseEntity.ok(response);
    }

}
