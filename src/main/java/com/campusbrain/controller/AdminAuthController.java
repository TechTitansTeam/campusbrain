package com.campusbrain.controller;

import com.campusbrain.dto.LoginRequest;
import com.campusbrain.dto.LoginResponse;
import com.campusbrain.entity.AdminUser;
import com.campusbrain.repository.AdminUserRepository;
import com.campusbrain.security.AuthTokenService;
import com.campusbrain.security.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordService passwordService;
    private final AuthTokenService tokenService;

    public AdminAuthController(AdminUserRepository adminUserRepository,
                               PasswordService passwordService,
                               AuthTokenService tokenService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Username and password are required."));
        }

        Optional<AdminUser> userOpt = adminUserRepository.findByUsername(request.getUsername().trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid administrator credentials."));
        }

        AdminUser user = userOpt.get();
        boolean valid = passwordService.verifyPassword(request.getPassword(), user.getSalt(), user.getPasswordHash());
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid administrator credentials."));
        }

        String token = tokenService.createToken(user.getUsername());
        LoginResponse response = new LoginResponse(
                token,
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                tokenService.getTokenValiditySeconds()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAdmin(HttpServletRequest request) {
        String username = (String) request.getAttribute("adminUsername");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Optional<AdminUser> userOpt = adminUserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Admin user not found"));
        }

        AdminUser user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "fullName", user.getFullName() != null ? user.getFullName() : user.getUsername(),
                "email", user.getEmail() != null ? user.getEmail() : ""
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else {
            token = request.getHeader("X-Admin-Token");
        }

        if (token != null) {
            tokenService.invalidateToken(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
