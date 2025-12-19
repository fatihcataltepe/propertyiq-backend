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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private SignupRequest validSignupRequest;
    private LoginRequest validLoginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validSignupRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .build();

        savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .emailVerified(false)
                .subscriptionTier("free")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validLoginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void signup_WithValidRequest_ShouldReturnUserResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.signup(validSignupRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");
        assertThat(response.isEmailVerified()).isFalse();
        assertThat(response.getSubscriptionTier()).isEqualTo("free");
        assertThat(response.getId()).isNotNull();

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void signup_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(validSignupRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should normalize email to lowercase and use normalized email for duplicate check")
    void signup_WithUppercaseEmail_ShouldNormalizeToLowercase() {
        SignupRequest requestWithUppercaseEmail = SignupRequest.builder()
                .email("TEST@EXAMPLE.COM")
                .password("password123")
                .name("Test User")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.signup(requestWithUppercaseEmail);

        // Verify existsByEmail is called with normalized (lowercase) email
        verify(userRepository).existsByEmail("test@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should trim whitespace from name")
    void signup_WithWhitespaceInName_ShouldTrimName() {
        SignupRequest requestWithWhitespaceName = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("  Test User  ")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.signup(requestWithWhitespaceName);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should hash password before saving")
    void signup_ShouldHashPassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.signup(validSignupRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
    }

    @Test
    @DisplayName("Should set default values for new user")
    void signup_ShouldSetDefaultValues() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            // Simulate @PrePersist behavior
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        authService.signup(validSignupRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.isEmailVerified()).isFalse();
        assertThat(capturedUser.getSubscriptionTier()).isEqualTo("free");
        // Note: createdAt and updatedAt are set by @PrePersist during actual persistence
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        LoginResponse response = authService.login(validLoginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void login_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void login_WithIncorrectPassword_ShouldThrowException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should normalize email to lowercase during login")
    void login_WithUppercaseEmail_ShouldNormalizeToLowercase() {
        LoginRequest requestWithUppercaseEmail = LoginRequest.builder()
                .email("TEST@EXAMPLE.COM")
                .password("password123")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        authService.login(requestWithUppercaseEmail);

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should trim whitespace from email during login")
    void login_WithWhitespaceInEmail_ShouldTrimEmail() {
        LoginRequest requestWithWhitespaceEmail = LoginRequest.builder()
                .email("  test@example.com  ")
                .password("password123")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        authService.login(requestWithWhitespaceEmail);

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should successfully logout and blacklist token")
    void logout_WithValidToken_ShouldBlacklistToken() {
        String token = "valid-jwt-token";
        String jti = "test-jti-123";
        Date expiration = new Date(System.currentTimeMillis() + 3600000);

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn(jti);
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtService.parseToken(token)).thenReturn(claims);

        boolean result = authService.logout(token);

        assertThat(result).isTrue();
        verify(jwtService).parseToken(token);
        verify(tokenBlacklistService).blacklistToken(eq(jti), anyLong());
    }

    @Test
    @DisplayName("Should return false when token is null")
    void logout_WithNullToken_ShouldReturnFalse() {
        boolean result = authService.logout(null);

        assertThat(result).isFalse();
        verify(jwtService, never()).parseToken(anyString());
        verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should return false when token is empty")
    void logout_WithEmptyToken_ShouldReturnFalse() {
        boolean result = authService.logout("");

        assertThat(result).isFalse();
        verify(jwtService, never()).parseToken(anyString());
        verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should return false when token is invalid")
    void logout_WithInvalidToken_ShouldReturnFalse() {
        String token = "invalid-token";
        when(jwtService.parseToken(token)).thenReturn(null);

        boolean result = authService.logout(token);

        assertThat(result).isFalse();
        verify(jwtService).parseToken(token);
        verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should return true when token is already expired")
    void logout_WithExpiredToken_ShouldReturnTrueWithoutBlacklisting() {
        String token = "expired-token";
        String jti = "test-jti-123";
        Date expiration = new Date(System.currentTimeMillis() - 1000);

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn(jti);
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtService.parseToken(token)).thenReturn(claims);

        boolean result = authService.logout(token);

        assertThat(result).isTrue();
        verify(jwtService).parseToken(token);
        verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should return false when token has no JTI")
    void logout_WithTokenWithoutJti_ShouldReturnFalse() {
        String token = "token-without-jti";

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn(null);
        when(jwtService.parseToken(token)).thenReturn(claims);

        boolean result = authService.logout(token);

        assertThat(result).isFalse();
        verify(jwtService).parseToken(token);
        verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
    }
}
