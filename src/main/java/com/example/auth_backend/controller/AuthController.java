package com.example.auth_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_backend.dto.UserCreateDTO;
import com.example.auth_backend.dto.UserDTO;
import com.example.auth_backend.service.AuthService;

@RestController
@RequestMapping("/user")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserCreateDTO userCreateDTO) {
        UserDTO created = authService.registerUser(userCreateDTO);
        return ResponseEntity.ok(created);
    }
}
