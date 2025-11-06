package com.example.auth_backend.mapper;

import com.example.auth_backend.dto.UserCreateDTO;
import com.example.auth_backend.dto.UserDTO;
import com.example.auth_backend.entity.User;

public class UserMapper {
    public static UserDTO mapToUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public static User mapToUser(UserCreateDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public static User mapToUser(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setEmail(dto.getEmail());
        user.setCreatedAt(dto.getCreatedAt());
        return user;
    }
}
