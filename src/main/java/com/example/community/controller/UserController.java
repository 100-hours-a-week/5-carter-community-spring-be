package com.example.community.controller;

import com.example.community.dto.UserDTO;
import com.example.community.model.User;
import com.example.community.repository.UserRepository;
import com.example.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

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


//    public ResponseEntity<String> register(
//            @RequestPart("user") UserDTO userDTO,
//            @RequestPart("image") MultipartFile imageFile) {
//        System.out.println("회원가입 시도");
//
//        try {
//            userService.register(userDTO, imageFile);
//            System.out.println("회원가입 성공");
//            return ResponseEntity.ok("User registered successfully");
//        } catch (IOException e) {
//            System.out.println("회원가입 실패");
//            return ResponseEntity.status(500).body("Failed to register user");
//        }
//    }



}
