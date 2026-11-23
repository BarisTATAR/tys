package com.tys.controller;

import com.tys.request.LoginRequest;
import com.tys.response.LoginResponse;
import com.tys.security.AuthCookie;
import com.tys.security.LoginGuard;
import com.tys.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final LoginGuard loginGuard;
    private final AuthCookie authCookie;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        String username = request != null ? request.getUsername() : null;
        try {
            loginGuard.assertAllowed(username);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(new LoginResponse(false, ex.getReason(), null));
        }
        LoginResponse response = adminService.login(request);
        if (response.isSuccess()) {
            loginGuard.recordSuccess(username);
            if (response.getToken() != null && !response.getToken().isBlank()) {
                authCookie.write(httpRequest, httpResponse, response.getToken());
                response.setToken(null);
            }
            return ResponseEntity.ok(response);
        }
        loginGuard.recordFailure(username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
