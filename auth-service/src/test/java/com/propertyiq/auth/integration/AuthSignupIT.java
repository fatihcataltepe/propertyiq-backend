package com.propertyiq.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.propertyiq.auth.dto.SignupRequest;
import com.propertyiq.auth.entity.User;
import com.propertyiq.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthSignupIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("auth_db_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register a new user and persist to database")
    void signup_WithValidRequest_ShouldReturn201AndPersistUser() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .name("New User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.name").value("New User"))
                .andExpect(jsonPath("$.data.emailVerified").value(false))
                .andExpect(jsonPath("$.data.subscriptionTier").value("free"))
                .andExpect(jsonPath("$.data.id").exists());

        Optional<User> savedUser = userRepository.findByEmail("newuser@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("New User");
        assertThat(passwordEncoder.matches("password123", savedUser.get().getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Should normalize email to lowercase before saving")
    void signup_WithUppercaseEmail_ShouldNormalizeAndPersist() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("UPPERCASE@EXAMPLE.COM")
                .password("password123")
                .name("Uppercase User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("uppercase@example.com"));

        Optional<User> savedUser = userRepository.findByEmail("uppercase@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("uppercase@example.com");
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void signup_WithExistingEmail_ShouldReturn409() throws Exception {
        User existingUser = User.builder()
                .email("existing@example.com")
                .passwordHash("hashedPassword")
                .name("Existing User")
                .build();
        userRepository.save(existingUser);

        SignupRequest request = SignupRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .name("New User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User with email 'existing@example.com' already exists"));
    }

    @Test
    @DisplayName("Should return 409 for case-variant duplicate email")
    void signup_WithCaseVariantDuplicateEmail_ShouldReturn409() throws Exception {
        User existingUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("Existing User")
                .build();
        userRepository.save(existingUser);

        SignupRequest request = SignupRequest.builder()
                .email("TEST@EXAMPLE.COM")
                .password("password123")
                .name("New User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User with email 'test@example.com' already exists"));
    }

    @Test
    @DisplayName("Should return 400 for invalid email format")
    void signup_WithInvalidEmail_ShouldReturn400() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("invalid-email")
                .password("password123")
                .name("Test User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 for password too short")
    void signup_WithShortPassword_ShouldReturn400() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .password("short")
                .name("Test User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void signup_WithMissingFields_ShouldReturn400() throws Exception {
        String incompleteJson = "{\"email\": \"test@example.com\"}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should hash password with BCrypt before storing")
    void signup_ShouldHashPasswordWithBCrypt() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("bcrypt@example.com")
                .password("mySecurePassword123")
                .name("BCrypt User")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Optional<User> savedUser = userRepository.findByEmail("bcrypt@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPasswordHash()).startsWith("$2a$");
        assertThat(savedUser.get().getPasswordHash()).isNotEqualTo("mySecurePassword123");
        assertThat(passwordEncoder.matches("mySecurePassword123", savedUser.get().getPasswordHash())).isTrue();
    }
}
