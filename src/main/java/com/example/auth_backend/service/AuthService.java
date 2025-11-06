package com.example.auth_backend.service;

import com.example.auth_backend.dto.LoginRequestDTO;
import com.example.auth_backend.dto.LoginResponseDTO;
import com.example.auth_backend.dto.UserCreateDTO;
import com.example.auth_backend.dto.UserDTO;

public interface AuthService {
    UserDTO registerUser(UserCreateDTO userCreateDTO);
    LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO);
}
