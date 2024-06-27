package com.example.community.controller;

import com.example.community.dto.UserDTO;
import com.example.community.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * 사용자가 로그인 요청을 처리합니다.
     *
     * @param userDTO   사용자 로그인 정보 (이메일 및 비밀번호)
     * @param response  HTTP 응답 객체
     * @return          인증 토큰 또는 로그인 실패 메시지를 포함한 응답
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        log.info("로그인 요청");
        Optional<Map<String, String>> tokensOptional = authService.authenticate(userDTO.getEmail(), userDTO.getPassword());
        System.out.println(tokensOptional.isPresent());
        if (tokensOptional.isPresent()) {
            Map<String, String> tokens = tokensOptional.get();
            String refreshToken = tokens.get("refreshToken");

            // 리프레시 토큰을 쿠키에 저장
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 전송
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(tokens);
        } else {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "로그인 실패"));
        }
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
     *
     * @param request HTTP 요청
     * @return 새로운 액세스 토큰을 포함한 응답
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    Optional<String> newAccessToken = authService.refreshAccessToken(refreshToken);

                    if (newAccessToken.isPresent()) {
                        return ResponseEntity.ok(Collections.singletonMap("accessToken", newAccessToken.get()));
                    } else {
                        return ResponseEntity.status(401).body(Collections.singletonMap("message", "유효하지 않은 리프레시 토큰"));
                    }
                }
            }
        }
        return ResponseEntity.status(400).body(Collections.singletonMap("message", "리프레시 토큰 없음"));
    }


    /**
     * 사용자가 로그아웃 요청을 처리합니다.
     *
     * @param response  HTTP 응답 객체
     * @return          로그아웃 성공 메시지를 포함한 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        log.info("로그아웃 요청");

        try {
            // JWT 쿠키 삭제
            Cookie jwtCookie = new Cookie("jwt", null);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // HTTPS 환경에서만 전송
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(0); // 쿠키 즉시 만료

            // 리프레시 토큰 쿠키 삭제
            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0); // 쿠키 즉시 만료

            response.addCookie(jwtCookie);
            response.addCookie(refreshTokenCookie);

            log.info("로그아웃 성공");
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body("로그아웃 처리 중 오류가 발생했습니다.");
        }
    }
}