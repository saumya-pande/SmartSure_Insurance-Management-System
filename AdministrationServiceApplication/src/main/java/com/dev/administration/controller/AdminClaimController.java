package com.dev.administration.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/claims")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClaimController {
	
	@GetMapping
	public ResponseEntity<?> checkFunc(){
		return ResponseEntity.ok("Admin access check func");
	}
    
}