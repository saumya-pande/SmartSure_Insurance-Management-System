package com.dev.authentication.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.authentication.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
