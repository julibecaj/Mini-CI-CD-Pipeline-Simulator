package com.pipes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Objects for authentication endpoints (R8).
 * Kept as inner records to reduce file count.
 */
public class AuthDto {

    /** POST /api/auth/register */
    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 60)  String username,
            @Email  @NotBlank                   String email,
            @NotBlank @Size(min = 6, max = 100) String password
    ) {}

    /** POST /api/auth/login */
    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    /** Returned on successful login */
    public record LoginResponse(
            String token,
            String username,
            String email
    ) {}
}
