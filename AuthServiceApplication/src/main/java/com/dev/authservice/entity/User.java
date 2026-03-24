package com.dev.authservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // Maps to 'users' table in database
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // Primary key column
    private Long id;

    @Column(name = "full_name", nullable = false) // Full name of the user
    private String name;
    
    @Email(message = "Invalid email") //to ensure correct format of writing an email
    @Column(name = "email_address", nullable = false, unique = true) // Unique email for login
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(
    	    regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$",
    	    message = "Password must be at least 8 characters and contain: one uppercase, one lowercase, one number, one special character"
    	) //follows to standard of password strength for more security
    @Column(name = "password_hash", nullable = false) // Encrypted password
    private String password;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    @Column(name = "phone_number") // Optional phone number
    private String phone;
    
    @Size(max = 255, message = "Address cannot exceed 255 characters") //to ensure no abuse of this. 
    @Column(name = "residential_address") // Optional physical address
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false) // Role-based access control (CUSTOMER, ADMIN)
    @Builder.Default
    private Role role = Role.CUSTOMER; 
}
