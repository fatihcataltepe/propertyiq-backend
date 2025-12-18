package com.propertyiq.auth.controller;

import com.propertyiq.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        // TODO: Implement get current user logic
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("email", "user@example.com");
        user.put("name", "Sample User");
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable Long id) {
        // TODO: Implement get user by id logic
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("email", "user@example.com");
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
