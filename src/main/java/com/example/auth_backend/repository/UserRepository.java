package com.example.auth_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);
}
