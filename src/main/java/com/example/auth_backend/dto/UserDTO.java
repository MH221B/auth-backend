package com.example.auth_backend.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long userId;
    private String email;
    private LocalDate createdAt; 
    private String role;
}
