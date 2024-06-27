package com.example.community.service;

import com.example.community.model.User;
import com.example.community.repository.UserRepository;
import com.example.community.util.JwtUtil;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 사용자의 이메일과 비밀번호를 인증하고, 인증에 성공하면 JWT 토큰을 반환합니다.
     *
     * @param email    사용자의 이메일
     * @param password 사용자의 비밀번호
     * @return JWT 토큰(액세스 토큰과 리프레시 토큰)을 포함한 Optional 객체, 인증 실패 시에는 빈 Optional 객체
     *      */
    public Optional<Map<String, String>> authenticate(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        System.out.println(email);
        if (user.isPresent()) {
            log.info("사용자 비밀번호: {}", user.get().getPassword());
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                String accessToken = jwtUtil.generateToken(user.get().getEmail(), user.get().getUserId(), user.get().getNickname());
                String refreshToken = jwtUtil.generateRefreshToken(user.get().getEmail(), user.get().getUserId());
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);
                return Optional.of(tokens);
            }
        }
        return Optional.empty();
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰을 포함한 Optional 객체, 유효하지 않은 리프레시 토큰 시에는 빈 Optional 객체
     */
    public Optional<String> refreshAccessToken(String refreshToken) {
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            String email = jwtUtil.extractEmail(refreshToken);
            Long userId = jwtUtil.extractUserId(refreshToken);
            String newAccessToken = jwtUtil.generateToken(email, userId, "nickname"); // nickname 정보는 실제로 필요한 정보를 넣으세요
            return Optional.of(newAccessToken);
        }
        return Optional.empty();
    }
}
