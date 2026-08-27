package com.pythogorean_apis.pythogorean_apis.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class TestController {

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/test")
    public String test() {
        return "Authenticated!";
    }
}