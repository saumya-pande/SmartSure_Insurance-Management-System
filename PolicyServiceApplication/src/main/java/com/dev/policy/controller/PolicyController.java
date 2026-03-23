package com.dev.policy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

	@GetMapping("/dummy")
    public ResponseEntity<String> getDummyPolicy(
            @RequestHeader(value = "X-LoggedIn-User", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Guard: reject if gateway headers are missing (request bypassed gateway)
        if (username == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Access denied: missing identity headers");
        }

        // Example of role-based access
        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: insufficient role");
        }

        // Your actual business logic here
        return ResponseEntity.ok("Response from Policy Service for user: " + username);
    }
}
