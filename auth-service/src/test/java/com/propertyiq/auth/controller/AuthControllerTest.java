package com.propertyiq.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.propertyiq.auth.dto.LoginRequest;
import com.propertyiq.auth.dto.LoginResponse;
import com.propertyiq.auth.dto.SignupRequest;
import com.propertyiq.auth.dto.UserResponse;
import com.propertyiq.auth.exception.AuthExceptionHandler;
import com.propertyiq.auth.exception.EmailAlreadyExistsException;
import com.propertyiq.auth.exception.InvalidCredentialsException;
import com.propertyiq.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private SignupRequest validSignupRequest;
    private UserResponse userResponse;
    private LoginRequest validLoginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        validSignupRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .build();

        userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
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

        loginResponse = LoginResponse.builder()
                .accessToken("sample-jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userResponse)
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created when signup is successful")
    @WithMockUser
    void signup_WithValidRequest_ShouldReturn201() throws Exception {
        when(authService.signup(any(SignupRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.emailVerified").value(false))
                .andExpect(jsonPath("$.data.subscriptionTier").value("free"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when email already exists")
    @WithMockUser
    void signup_WithExistingEmail_ShouldReturn409() throws Exception {
        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("test@example.com"));

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User with email 'test@example.com' already exists"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is missing")
    @WithMockUser
    void signup_WithMissingEmail_ShouldReturn400() throws Exception {
        SignupRequest invalidRequest = SignupRequest.builder()
                .password("password123")
                .name("Test User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid")
    @WithMockUser
    void signup_WithInvalidEmailFormat_ShouldReturn400() throws Exception {
        SignupRequest invalidRequest = SignupRequest.builder()
                .email("invalid-email")
                .password("password123")
                .name("Test User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password is too short")
    @WithMockUser
    void signup_WithShortPassword_ShouldReturn400() throws Exception {
        SignupRequest invalidRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("short")
                .name("Test User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when name is missing")
    @WithMockUser
    void signup_WithMissingName_ShouldReturn400() throws Exception {
        SignupRequest invalidRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when name is blank")
    @WithMockUser
    void signup_WithBlankName_ShouldReturn400() throws Exception {
        SignupRequest invalidRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("   ")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 OK when login is successful")
    @WithMockUser
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("sample-jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.name").value("Test User"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when credentials are invalid")
    @WithMockUser
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login email is missing")
    @WithMockUser
    void login_WithMissingEmail_ShouldReturn400() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login password is missing")
    @WithMockUser
    void login_WithMissingPassword_ShouldReturn400() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login email format is invalid")
    @WithMockUser
    void login_WithInvalidEmailFormat_ShouldReturn400() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 OK when logout is successful")
    @WithMockUser
    void logout_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
