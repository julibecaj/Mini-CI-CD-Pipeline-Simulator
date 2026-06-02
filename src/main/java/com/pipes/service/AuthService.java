package com.pipes.service;

import com.pipes.dto.AuthDto;
import com.pipes.entity.User;
import com.pipes.repository.UserRepository;
import com.pipes.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and JWT-based login.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Register a new user.
     *
     * @throws IllegalArgumentException (unchecked, R10) if username/email taken
     */
    @Transactional
    public User register(AuthDto.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username '" + req.username() + "' is already taken.");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email '" + req.email() + "' is already registered.");
        }

        User user = new User(
                req.username(),
                req.email(),
                passwordEncoder.encode(req.password())
        );
        return userRepository.save(user);
    }

    /**
     * Authenticate and return a JWT response.
     * Spring Security throws BadCredentialsException on failure (handled by GlobalExceptionHandler).
     */
    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtils.generateToken(userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        return new AuthDto.LoginResponse(token, user.getUsername(), user.getEmail());
    }
}
