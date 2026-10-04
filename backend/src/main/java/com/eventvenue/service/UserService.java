package com.eventvenue.service;

import com.eventvenue.dto.LoginRequest;
import com.eventvenue.dto.RegisterRequest;
import com.eventvenue.entity.User;
import com.eventvenue.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest request) {
        String normalizedUsername = normalizeUsername(request.getUsername());
        String email = request.getEmail() == null ? null : request.getEmail().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("This username is already taken.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setUsername(normalizedUsername);
        user.setEmail(email);
        user.setPassword(request.getPassword());

        String role = request.getRole() == null ? "USER" : request.getRole().toUpperCase();
        if (!"ADMIN".equals(role) && !"USER".equals(role) && !"STUDENT".equals(role)) {
            throw new IllegalArgumentException("Unsupported role. Use ADMIN or USER.");
        }
        user.setRole("STUDENT".equals(role) ? "USER" : role);

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim();

        User user = null;
        if (username != null && !username.isEmpty()) {
            user = userRepository.findByUsername(username)
                    .orElse(null);
        }

        if (user == null && email != null && !email.isEmpty()) {
            user = userRepository.findByEmail(email)
                    .orElse(null);
        }

        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        return username.trim();
    }
}
