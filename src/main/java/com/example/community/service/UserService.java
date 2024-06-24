package com.example.community.service;

import com.example.community.dto.UserDTO;
import com.example.community.model.User;
import com.example.community.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
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

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}
