package com.pipes.controller;

import com.pipes.dto.AuthDto;
import com.pipes.entity.User;
import com.pipes.service.AuthService;
import com.pipes.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for authentication (R8).
 *
 * POST /api/auth/register  — create a new account
 * POST /api/auth/login     — authenticate and receive a JWT
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody AuthDto.RegisterRequest req) {

        User user = authService.register(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created. Welcome, " + user.getUsername() + "!", user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest req) {

        AuthDto.LoginResponse resp = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }
}
