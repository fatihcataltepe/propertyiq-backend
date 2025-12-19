package com.propertyiq.auth.repository;

import com.propertyiq.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository using external PostgreSQL (docker-compose).
 * Run with: ./gradlew :auth-service:test --tests "*LocalIT" -Dspring.profiles.active=integration-local
 * 
 * Prerequisites: docker-compose up -d (PostgreSQL must be running on localhost:5432)
 */
@DataJpaTest
@ActiveProfiles("integration-local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryLocalIT {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find user by email")
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        User user = User.builder()
                .email("existing@example.com")
                .passwordHash("hashedPassword")
                .name("Existing User")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("existing@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should persist user with all fields")
    void save_WithValidUser_ShouldPersistAllFields() {
        User user = User.builder()
                .email("complete@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .name("Complete User")
                .emailVerified(false)
                .subscriptionTier("free")
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("complete@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
        assertThat(saved.getName()).isEqualTo("Complete User");
        assertThat(saved.isEmailVerified()).isFalse();
        assertThat(saved.getSubscriptionTier()).isEqualTo("free");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void save_WithDuplicateEmail_ShouldThrowException() {
        User user1 = User.builder()
                .email("duplicate@example.com")
                .passwordHash("hashedPassword1")
                .name("User 1")
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .email("duplicate@example.com")
                .passwordHash("hashedPassword2")
                .name("User 2")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> {
                    userRepository.save(user2);
                    userRepository.flush();
                }
        );
    }
}
