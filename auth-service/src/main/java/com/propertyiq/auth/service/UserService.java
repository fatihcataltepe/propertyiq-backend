package com.propertyiq.auth.service;

import com.propertyiq.auth.dto.UpdateUserRequest;
import com.propertyiq.auth.dto.UserResponse;
import com.propertyiq.auth.entity.User;
import com.propertyiq.auth.exception.UserNotFoundException;
import com.propertyiq.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }
}
