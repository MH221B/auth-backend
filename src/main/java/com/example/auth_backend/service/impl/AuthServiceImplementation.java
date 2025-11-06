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

@Service
@AllArgsConstructor
public class AuthServiceImplementation implements AuthService {
    private final UserRepository userRepository;

    @Override
    public UserDTO registerUser(UserCreateDTO userCreateDTO) {
        if (userCreateDTO == null) return null;
        String email = userCreateDTO.getEmail();
        if (email != null && userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email already in use: " + email);
        }

        User user = UserMapper.mapToUser(userCreateDTO);
        User saved = userRepository.save(user);
        return UserMapper.mapToUserDTO(saved);
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        return null;
    }
}
