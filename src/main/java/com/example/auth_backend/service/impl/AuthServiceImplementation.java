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
import com.example.auth_backend.service.JwtService;
import com.example.auth_backend.service.RefreshTokenService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class AuthServiceImplementation implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

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
        user.setPassword(passwordEncoder.encode(password));

        User saved = userRepository.save(user);
        return UserMapper.mapToUserDTO(saved);
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        if (loginRequestDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();

        if (email == null || email.trim().isEmpty() || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        User user = userRepository.findAll().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

    // generate access token
        String accessToken = jwtService.generateAccessToken(user);
    // create refresh token raw value (stored hashed in DB by service)
    String refreshRaw = refreshTokenService.createToken(user);

    LoginResponseDTO resp = new LoginResponseDTO();
        resp.setToken(accessToken);
        resp.setUser(UserMapper.mapToUserDTO(user));
    resp.setRefreshTokenRaw(refreshRaw);
        // NOTE: refresh token raw is intentionally not returned in body — it will be set as httpOnly cookie by controller
        return resp;
    }
}
