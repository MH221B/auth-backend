package com.example.auth_backend.service.impl;

import com.example.auth_backend.dto.LoginRequestDTO;
import com.example.auth_backend.dto.LoginResponseDTO;
import com.example.auth_backend.dto.UserCreateDTO;
import com.example.auth_backend.dto.UserDTO;
import com.example.auth_backend.exception.ResourceAlreadyExistsException;
import com.example.auth_backend.mapper.UserMapper;
import com.example.auth_backend.entity.User;
import com.example.auth_backend.repository.UserRepository;
import com.example.auth_backend.service.AuthService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class AuthServiceImplementation implements AuthService {
    private final UserRepository userRepository;

    @Override
    public UserDTO registerUser(UserCreateDTO userCreateDTO) {
        if (userCreateDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String email = userCreateDTO.getEmail();
        String password = userCreateDTO.getPassword();

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        // Simple email format check
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email already in use: " + email);
        }

        User user = UserMapper.mapToUser(userCreateDTO);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(password));

        User saved = userRepository.save(user);
        return UserMapper.mapToUserDTO(saved);
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        return null;
    }
}
