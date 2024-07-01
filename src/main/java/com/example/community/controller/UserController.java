package com.example.community.controller;

import com.example.community.dto.UserDTO;
import com.example.community.model.User;
import com.example.community.service.UserService;
import com.example.community.util.JwtUtil;


import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.net.MalformedURLException;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 새로운 유저를 등록합니다.
     *
     * @param email    유저 이메일
     * @param password 유저 비밀번호
     * @param nickname 유저 닉네임
     * @param imageFile 프로필 이미지 파일
     * @return 상태 코드 200 (성공) 또는 오류 메시지
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam("email") String email,
                                           @RequestParam("password") String password,
                                           @RequestParam("nickname") String nickname,
                                           @RequestParam("image") MultipartFile imageFile) {
        log.info("회원가입 시도");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setPassword(password);
        userDTO.setNickname(nickname);
        try {
            userService.register(userDTO, imageFile);
            log.info("회원가입 성공");
            return ResponseEntity.ok("가입 성공");
        } catch (IOException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body("가입 실패");
        }
    }

    /**
     * 유저 비밀번호를 수정합니다.
     *
     * @param request 요청 데이터 (새로운 비밀번호 포함)
     * @param token   JWT 토큰
     * @return 상태 코드 200 (성공)
     */
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token) {
        log.info("비밀번호 수정 시도");
        String newPassword = request.get("password");
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            userService.updatePassword(userId, newPassword);
            log.info("비밀번호 수정 성공");
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.error("비밀번호 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(404).body(null);
        }
    }

    /**
     * 유저 프로필을 수정합니다.
     *
     * @param nickname  새로운 닉네임 (선택 사항)
     * @param imageFile 새로운 프로필 이미지 파일 (선택 사항)
     * @param token     JWT 토큰
     * @return 상태 코드 200 (성공) 또는 오류 메시지
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("Authorization") String token) {
        log.info("프로필 수정 시도");
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            userService.updateUserProfile(userId, nickname, imageFile);
            log.info("프로필 수정 성공");
            return ResponseEntity.ok("Profile updated successfully");
        } catch (IOException e) {
            log.error("프로필 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to update profile");
        } catch (IllegalArgumentException e) {
            log.error("프로필 수정 실패 - 존재하지 않는 유저: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 유저를 삭제합니다.
     *
     * @param token JWT 토큰
     * @return 상태 코드 200 (성공) 또는 오류 메시지
     */
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String token){
        log.info("회원 삭제 요청");

        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            userService.deleteUserById(userId);
            log.info("회원 삭제 성공");
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            log.error("회원 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to delete user");
        }
    }

    /**
     * 유저 프로필 이미지를 가져옵니다.
     *
     * @param userId 유저 ID (선택 사항)
     * @param token  JWT 토큰 (선택 사항)
     * @return 이미지 리소스
     */
    @GetMapping({"/image", "/{userId}/image"})
    public ResponseEntity<Resource> getUserImage(
            @PathVariable(required = false) Long userId,
            @RequestHeader(value = "Authorization",required = false) String token) {
        log.info("이미지 요청: {}", userId);

        if (userId == null && token != null) {
            userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        } else if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
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
            log.error("이미지 요청 실패 - 서버 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 현재 유저의 ID를 가져옵니다.
     *
     * @param token JWT 토큰
     * @return 유저 ID
     */
    @GetMapping("/userId")
    public ResponseEntity<Long> getUserId(@RequestHeader("Authorization") String token) {
        log.info("유저 아이디 요청");
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            log.info("유저 아이디: {}", userId);
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            log.error("유저 아이디 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
    }

    /**
     * 현재 유저의 닉네임을 가져옵니다.
     *
     * @param userId 유저 ID (선택 사항)
     * @param token  JWT 토큰 (선택 사항)
     * @return 닉네임
     */
    @GetMapping({"/nickname", "/{userId}/nickname"})
    public ResponseEntity<String> getNickname(
            @PathVariable(required = false) Long userId,
            @RequestHeader("Authorization") String token) {
        log.info("닉네임 요청");
        try {
            if (userId == null && token != null) {
                userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            } else if (userId == null) {
                return ResponseEntity.badRequest().body("User ID is missing and token is not provided");
            }

            String nickname = userService.getNicknameByUserId(userId);
            log.info("닉네임: {}", nickname);
            return ResponseEntity.ok(nickname);
        } catch (IllegalArgumentException e) {
            log.error("닉네임 요청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("서버 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 현재 유저의 이메일을 가져옵니다.
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    @GetMapping("/email")
    public ResponseEntity<String> getEmail(@RequestHeader("Authorization") String token) {
        log.info("이메일 요청");
        try {
            Long userId = userService.extractUserIdFromToken(token.replace("Bearer ", ""));
            String email = userService.getEmailByUserId(userId);
            log.info("이메일: {}", email);
            return ResponseEntity.ok(email);
        } catch (IllegalArgumentException e) {
            log.error("이메일 요청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 닉네임 중복 여부를 확인합니다.
     *
     * @param nickname 닉네임
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<Boolean> checkNickname(@PathVariable String nickname) {
        log.info("닉네임 중복 체크 요청: {}", nickname);
        boolean isTaken = userService.isNicknameTaken(nickname);
        System.out.println(isTaken);
        return ResponseEntity.ok(isTaken);
    }

    /**
     * 이메일 중복 여부를 확인합니다.
     *
     * @param email 이메일
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        log.info("이메일 중복 체크 요청: {}", email);
        boolean isTaken = userService.isEmailTaken(email);
        return ResponseEntity.ok(isTaken);
    }
}