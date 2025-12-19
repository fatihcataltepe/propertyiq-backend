package com.propertyiq.auth.service;

import com.propertyiq.auth.dto.SignupRequest;
import com.propertyiq.auth.dto.UserResponse;
import com.propertyiq.auth.entity.User;
import com.propertyiq.auth.exception.EmailAlreadyExistsException;
import com.propertyiq.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }
}
