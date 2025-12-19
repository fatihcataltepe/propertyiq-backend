package com.propertyiq.auth.service;

import com.propertyiq.auth.dto.UpdateUserRequest;
import com.propertyiq.auth.dto.UserResponse;
import com.propertyiq.auth.entity.User;
import com.propertyiq.auth.exception.UserNotFoundException;
import com.propertyiq.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .emailVerified(false)
                .subscriptionTier("free")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return user when user exists")
    void getUserById_WithExistingUser_ShouldReturnUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserResponse response = userService.getUserById(userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");
        assertThat(response.isEmailVerified()).isFalse();
        assertThat(response.getSubscriptionTier()).isEqualTo("free");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void getUserById_WithNonExistingUser_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should update user name successfully")
    void updateUser_WithValidName_ShouldUpdateAndReturnUserResponse() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Should trim whitespace from name when updating")
    void updateUser_WithWhitespaceInName_ShouldTrimName() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("  Updated Name  ")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(userId, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Should not update name when name is null")
    void updateUser_WithNullName_ShouldNotUpdateName() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(userId, request);

        assertThat(response.getName()).isEqualTo("Test User");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should not update name when name is blank")
    void updateUser_WithBlankName_ShouldNotUpdateName() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("   ")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(userId, request);

        assertThat(response.getName()).isEqualTo("Test User");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating non-existing user")
    void updateUser_WithNonExistingUser_ShouldThrowException() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
