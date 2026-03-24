package com.dev.admin.controller;

import com.dev.admin.client.AuthServiceAdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AuthServiceAdminClient authClient;

    @GetMapping
    public ResponseEntity<List<Object>> getAllUsers() {
        return ResponseEntity.ok(authClient.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(authClient.getUserById(userId));
    }
}
