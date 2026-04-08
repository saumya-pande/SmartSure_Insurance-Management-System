package com.dev.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dev.authentication.entity.User;
import com.dev.authentication.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    long countByActive(boolean active);

    @Query("SELECT u FROM User u WHERE " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:name IS NULL OR u.name LIKE %:name%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:active IS NULL OR u.active = :active)")
    Page<User> findByFilters(
            @Param("email") String email,
            @Param("name") String name,
            @Param("role") Role role,
            @Param("active") Boolean active,
            Pageable pageable);

}
