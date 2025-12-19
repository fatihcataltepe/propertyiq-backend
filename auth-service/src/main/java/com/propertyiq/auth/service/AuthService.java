package com.propertyiq.auth.service;

import com.propertyiq.auth.dto.LoginRequest;
import com.propertyiq.auth.dto.LoginResponse;
import com.propertyiq.auth.dto.SignupRequest;
import com.propertyiq.auth.dto.UserResponse;
import com.propertyiq.auth.entity.User;
import com.propertyiq.auth.exception.EmailAlreadyExistsException;
import com.propertyiq.auth.exception.InvalidCredentialsException;
import com.propertyiq.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public boolean logout(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        Claims claims = jwtService.parseToken(token);
        if (claims == null) {
            return false;
        }

        String jti = claims.getId();
        if (jti == null || jti.isEmpty()) {
            return false;
        }

        Date expiration = claims.getExpiration();
        if (expiration == null || expiration.before(new Date())) {
            return true;
        }

        long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        if (ttlSeconds > 0) {
            tokenBlacklistService.blacklistToken(jti, ttlSeconds);
        }

        return true;
    }
}
