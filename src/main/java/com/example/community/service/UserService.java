package com.example.community.service;

import com.example.community.dto.UserDTO;
import com.example.community.model.User;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;

import com.example.community.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private static final String USER_IMAGE_DIRECTORY = "src/main/resources/images/user/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
    }

    public User register(UserDTO userDTO, MultipartFile imageFile) throws IOException {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setNickname(userDTO.getNickname());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = UUID.randomUUID().toString() + "." + getFileExtension(imageFile.getOriginalFilename());
            Path imagePath = Paths.get(USER_IMAGE_DIRECTORY + imageName);
            Files.write(imagePath, imageFile.getBytes());
            user.setImage(imageName);
            userDTO.setImage(imageName); // DTO에 이미지 이름 설정
        }

        return userRepository.save(user);
    }

    public User updatePassword(Long userId, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        } else {
            throw new IllegalStateException("User not found");
        }
    }

    public void updateUserProfile(Long userId, String nickname, MultipartFile imageFile) throws IOException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (nickname != null && !nickname.isEmpty()) {
                user.setNickname(nickname);
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                System.out.println("덮어쓰기직전");
                // 기존 이미지 경로를 사용하여 덮어쓰기
                String imageName = user.getImage();
                Path imagePath = Paths.get(USER_IMAGE_DIRECTORY + imageName);
                Files.write(imagePath, imageFile.getBytes());
            }
            userRepository.save(user);
        } else {
            throw new IllegalStateException("User not found");
        }
    }

    @Transactional
    public void deleteUserById(Long userId) {
        commentRepository.deleteCommentsByUserId(userId);
        postRepository.deletePostsByUserId(userId);
        userRepository.deleteById(userId);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    public Resource loadUserImage(String imageName) throws MalformedURLException {
        Path imagePath = Paths.get(USER_IMAGE_DIRECTORY).resolve(imageName).normalize();
        return new UrlResource(imagePath.toUri());
    }

    public String getNicknameByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getNickname();
    }

    public String getEmailByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getEmail();
    }

    public Long extractUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }

    public boolean isNicknameTaken(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}