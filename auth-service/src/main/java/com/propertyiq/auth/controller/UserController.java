package com.propertyiq.auth.controller;

import com.propertyiq.auth.dto.UpdateUserRequest;
import com.propertyiq.auth.dto.UserResponse;
import com.propertyiq.auth.service.UserService;
import com.propertyiq.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader("X-User-Id") String userId) {
        UserResponse user = userService.getUserById(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", user));
    }
}
