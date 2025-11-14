package com.example.auth_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_backend.entity.User;
import com.example.auth_backend.entity.Role;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);
	List<User> findByRole(Role role);
}
