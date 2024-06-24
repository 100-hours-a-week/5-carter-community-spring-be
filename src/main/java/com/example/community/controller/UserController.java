package com.example.community.controller;

import com.example.community.dto.UserDTO;
import com.example.community.model.User;
import com.example.community.repository.UserRepository;
import com.example.community.service.UserService;
import com.example.community.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam("email") String email,
                                           @RequestParam("password") String password,
                                           @RequestParam("nickname") String nickname,
                                           @RequestParam("image") MultipartFile imageFile) {
        System.out.println("회원가입 시도");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setPassword(password);
        userDTO.setNickname(nickname);
        try {
            userService.register(userDTO, imageFile);
            return ResponseEntity.ok("가입 성공");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("가입 실패");
        }
    }


    @PutMapping("/{userId}/password")
    public ResponseEntity<User> updatePassword(@PathVariable Long userId, @RequestBody Map<String, String> request,@RequestHeader("Authorization") String token) {
        System.out.println("비번수정시도");
        String newPassword = request.get("password");
        try {
            Long tokenUserId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            if (!userId.equals(tokenUserId)) {
                return ResponseEntity.status(403).body(null);
            }
            User updatedUser = userService.updatePassword(userId, newPassword);
            System.out.println("비번수정성공");
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException e) {
            System.out.println("비번수정실패");
            return ResponseEntity.status(404).body(null);
        }
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<String> updateUserProfile(
            @PathVariable Long userId,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        System.out.println("프로필수정시도");
        try {



            userService.updateUserProfile(userId, nickname, imageFile);
            System.out.println("프로필수정성공");
            return ResponseEntity.ok("Profile updated successfully");
        } catch (IOException e) {
            System.out.println("프로필수정실패");
            return ResponseEntity.status(500).body("Failed to update profile");
        } catch (IllegalStateException e) {
            System.out.println("프로필수정실패-존재하지않는유저");
            return ResponseEntity.status(404).body("User not found");
        }
    }


}