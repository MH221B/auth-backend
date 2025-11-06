package com.example.auth_backend.dto;

import lombok.Data;

@Data
public class UserCreateDTO {
    private String email;
    private String password;
}
