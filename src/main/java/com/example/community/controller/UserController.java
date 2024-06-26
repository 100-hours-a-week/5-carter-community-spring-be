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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final String USER_IMAGE_DIRECTORY = "src/main/resources/images/user/";

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


    @PutMapping("/password")
    public ResponseEntity<User> updatePassword(
            @RequestBody Map<String, String> request,
            @CookieValue("jwt") String token) {
        System.out.println("비번수정시도");
        String newPassword = request.get("password");
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User updatedUser = userService.updatePassword(userId, newPassword);
            System.out.println("비번수정성공");
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException e) {
            System.out.println("비번수정실패");
            return ResponseEntity.status(404).body(null);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @CookieValue(value = "jwt", required = false) String token) {
        System.out.println("프로필수정시도");

        if (token == null) {
            return ResponseEntity.badRequest().body("Token is missing");
        }

        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            userService.updateUserProfile(userId, nickname, imageFile);
            System.out.println("프로필수정성공");

            return ResponseEntity.ok("Profile updated successfully");
        } catch (IOException e) {
            System.out.println("프로필수정실패");
            return ResponseEntity.status(500).body("Failed to update profile");
        } catch (IllegalArgumentException e) {
            System.out.println("프로필수정실패-존재하지않는유저");

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping
    public ResponseEntity<String> deleteUser(@CookieValue("jwt") String token){
        System.out.println("회원 삭제 요청");

        try{
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));

            userService.deleteUserById(userId);
            System.out.println("회원 삭제 성공");
            return ResponseEntity.ok("User deleted successfully");
        }
        catch (Exception e) {
            System.out.println("회원 삭제 실패");
            return ResponseEntity.status(500).body("Failed to update profile");
        }

    }

    @GetMapping({"/image", "/{userId}/image"})
    public ResponseEntity<Resource> getUserImage(
            @PathVariable(required = false) Long userId,
            @CookieValue(value = "jwt", required = false) String token) {
        if (userId == null && token != null) {
            userId = jwtUtil.extractUserId(token);
        } else if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        System.out.println("이미지요청");
        User user = userService.getUserById(userId);
        String imageName = user.getImage();

        try {
            Resource resource = userService.loadUserImage(imageName);

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/userId")
    public ResponseEntity<Long> getUserId(@CookieValue(value = "jwt", required = false) String token) {
        System.out.println("유저아이디요청");
        if (token == null) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Long userId = jwtUtil.extractUserId(token);
            System.out.println(userId);
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
    }

    @GetMapping({"/nickname", "/{userId}/nickname"})
    public ResponseEntity<String> getNickname(
            @PathVariable(required = false) Long userId,
            @CookieValue(value = "jwt", required = false) String token) {

        if (userId == null && token != null) {
            userId = jwtUtil.extractUserId(token);
        } else if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            String nickname = userService.getNicknameByUserId(userId);
            System.out.println(nickname);
            return ResponseEntity.ok(nickname);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/email")
    public ResponseEntity<String> getEmail(@CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Long userId = userService.extractUserIdFromToken(token);
            String email = userService.getEmailByUserId(userId);
            return ResponseEntity.ok(email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<Boolean> checkNickname(@PathVariable String nickname) {
        boolean isTaken = userService.isNicknameTaken(nickname);
        return ResponseEntity.ok(isTaken);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        System.out.println("이메일 중복 체크 요청");
        boolean isTaken = userService.isEmailTaken(email);
        return ResponseEntity.ok(isTaken);
    }

}