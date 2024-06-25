package com.example.community.controller;

import com.example.community.dto.LoginRequest;
import com.example.community.model.User;
import com.example.community.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        System.out.println("로그인 시도");
        Optional<String> tokenOptional = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        if (tokenOptional.isPresent()) {
            String token = tokenOptional.get();
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // HTTPS 환경에서만 전송
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 동안 유효
            response.addCookie(jwtCookie);

            System.out.println("로그인 성공");

            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("로그인 실패");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Logout successful");
    }
}