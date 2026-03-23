package com.dev.policy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @GetMapping("/dummy")
    public String getDummyPolicy() {
        return "Response from Policy Service";
    }
}
