package com.propertyiq.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.propertyiq.auth.dto.UpdateUserRequest;
import com.propertyiq.auth.dto.UserResponse;
import com.propertyiq.auth.exception.AuthExceptionHandler;
import com.propertyiq.auth.exception.UserNotFoundException;
import com.propertyiq.auth.service.UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(AuthExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UUID userId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .emailVerified(false)
                .subscriptionTier("free")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /users/me - Should return current user profile")
    @WithMockUser
    void getCurrentUser_WithValidUserId_ShouldReturnUser() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.emailVerified").value(false))
                .andExpect(jsonPath("$.data.subscriptionTier").value("free"));
    }

    @Test
    @DisplayName("GET /users/me - Should return 404 when user not found")
    @WithMockUser
    void getCurrentUser_WithNonExistingUser_ShouldReturn404() throws Exception {
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User with id '" + userId + "' not found"));
    }

    @Test
    @DisplayName("GET /users/{id} - Should return user by ID")
    @WithMockUser
    void getUserById_WithValidId_ShouldReturnUser() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    @DisplayName("GET /users/{id} - Should return 404 when user not found")
    @WithMockUser
    void getUserById_WithNonExistingId_ShouldReturn404() throws Exception {
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User with id '" + userId + "' not found"));
    }

    @Test
    @DisplayName("PUT /users/me - Should update user profile successfully")
    @WithMockUser
    void updateCurrentUser_WithValidRequest_ShouldReturnUpdatedUser() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated Name")
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("Updated Name")
                .emailVerified(false)
                .subscriptionTier("free")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User profile updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("PUT /users/me - Should return 404 when user not found")
    @WithMockUser
    void updateCurrentUser_WithNonExistingUser_ShouldReturn404() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated Name")
                .build();

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User with id '" + userId + "' not found"));
    }

    @Test
    @DisplayName("PUT /users/me - Should return 400 when name exceeds max length")
    @WithMockUser
    void updateCurrentUser_WithNameTooLong_ShouldReturn400() throws Exception {
        String longName = "a".repeat(256);
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name(longName)
                .build();

        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
