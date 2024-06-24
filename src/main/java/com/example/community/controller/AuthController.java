package com.example.community.controller;

import com.example.community.dto.LoginRequest;
import com.example.community.model.User;
import com.example.community.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("로그인 시도");
        Optional<String> user = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        if (user.isPresent()) {
            System.out.println("로그인 성공");
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(401).body("로그인 실패");
        }
    }
}