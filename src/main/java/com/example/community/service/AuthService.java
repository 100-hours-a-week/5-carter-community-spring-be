package com.example.community.service;

import com.example.community.model.User;
import com.example.community.repository.UserRepository;
import com.example.community.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Optional<String> authenticate(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            System.out.println(user.get().getPassword());
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                String token = jwtUtil.generateToken(user.get().getEmail(), user.get().getUserId(), user.get().getNickname());
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }
}
