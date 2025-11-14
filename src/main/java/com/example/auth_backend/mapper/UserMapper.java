package com.example.auth_backend.mapper;

import com.example.auth_backend.dto.UserCreateDTO;
import com.example.auth_backend.dto.UserDTO;
import com.example.auth_backend.entity.Role;
import com.example.auth_backend.entity.User;

public class UserMapper {
    public static UserDTO mapToUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        return dto;
    }

    public static User mapToUser(UserCreateDTO dto, boolean allowRole) {
        if (dto == null) return null;
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (allowRole && dto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                user.setRole(Role.USER);
            }
        } else {
            user.setRole(Role.USER);
        }
        return user;
    }

    public static User mapToUser(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setEmail(dto.getEmail());
        user.setCreatedAt(dto.getCreatedAt());
        if (dto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                user.setRole(Role.USER);
            }
        } else {
            user.setRole(Role.USER);
        }
        return user;
    }
}
