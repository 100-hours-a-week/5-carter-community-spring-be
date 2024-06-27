package com.example.community.service;

import com.example.community.dto.UserDTO;
import com.example.community.model.User;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import com.example.community.util.JwtUtil;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

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
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private static final String USER_IMAGE_DIRECTORY = "src/main/resources/images/user/";

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 유저 ID로 유저를 가져옵니다.
     *
     * @param userId 유저 ID
     * @return 유저
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
    }

    /**
     * 유저를 등록합니다.
     *
     * @param userDTO 유저 DTO
     * @param imageFile 이미지 파일
     * @return 등록된 유저
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public User register(UserDTO userDTO, MultipartFile imageFile) throws IOException {
        String imageName = UUID.randomUUID().toString() + "." + getFileExtension(imageFile.getOriginalFilename());
        Path imagePath = Paths.get(USER_IMAGE_DIRECTORY + imageName);
        Files.write(imagePath, imageFile.getBytes());

        User user = User.builder()
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .nickname(userDTO.getNickname())
                .image(imageName)
                .build();

        return userRepository.save(user);
    }

    /**
     * 유저의 비밀번호를 업데이트합니다.
     *
     * @param userId 유저 ID
     * @param newPassword 새로운 비밀번호
     * @return 업데이트된 유저
     */
    public User updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * 유저 프로필을 업데이트합니다.
     *
     * @param userId 유저 ID
     * @param nickname 닉네임
     * @param imageFile 이미지 파일
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public void updateUserProfile(Long userId, String nickname, MultipartFile imageFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = user.getImage();
            Path imagePath = Paths.get(USER_IMAGE_DIRECTORY + imageName);
            Files.write(imagePath, imageFile.getBytes());
        }
        userRepository.save(user);
    }

    /**
     * 유저를 삭제합니다.
     *
     * @param userId 유저 ID
     */
    @Transactional
    public void deleteUserById(Long userId) {
        commentRepository.deleteCommentsByUserId(userId);
        postRepository.deletePostsByUserId(userId);
        userRepository.deleteById(userId);
    }

    /**
     * 파일 확장자를 가져옵니다.
     *
     * @param fileName 파일 이름
     * @return 파일 확장자
     */
    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 이메일이 이미 존재하는지 확인합니다.
     *
     * @param email 이메일
     * @return 존재 여부
     */
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 유저 이미지 파일을 로드합니다.
     *
     * @param imageName 이미지 이름
     * @return 이미지 파일 리소스
     * @throws MalformedURLException 유효하지 않은 URL
     */
    public Resource loadUserImage(String imageName) throws MalformedURLException {
        Path imagePath = Paths.get(USER_IMAGE_DIRECTORY).resolve(imageName).normalize();
        return new UrlResource(imagePath.toUri());
    }

    /**
     * 유저 ID로 닉네임을 가져옵니다.
     *
     * @param userId 유저 ID
     * @return 닉네임
     */
    public String getNicknameByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getNickname();
    }

    /**
     * 유저 ID로 이메일을 가져옵니다.
     *
     * @param userId 유저 ID
     * @return 이메일
     */
    public String getEmailByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getEmail();
    }

    /**
     * 토큰에서 유저 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 유저 ID
     */
    public Long extractUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }

    /**
     * 닉네임이 이미 존재하는지 확인합니다.
     *
     * @param nickname 닉네임
     * @return 존재 여부
     */
    public boolean isNicknameTaken(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}