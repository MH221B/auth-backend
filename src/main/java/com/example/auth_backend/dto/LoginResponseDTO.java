package com.example.auth_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private UserDTO user;

    // raw refresh token is intentionally not serialized to the response body
    @JsonIgnore
    private String refreshTokenRaw;
}
