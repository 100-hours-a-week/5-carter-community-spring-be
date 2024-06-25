package com.example.community.controller;

import com.example.community.dto.PostDTO;
import com.example.community.model.Post;
import com.example.community.model.User;
import com.example.community.service.PostService;
import com.example.community.service.UserService;
import com.example.community.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<String> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart("image") MultipartFile imageFile,
            @RequestHeader("Authorization") String token) {

        System.out.println("게시글 추가 시도");
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);

        PostDTO postDTO = new PostDTO();
        postDTO.setUserId(userId);
        postDTO.setTitle(title);
        postDTO.setContent(content);
        postDTO.setDate(LocalDateTime.now());
        postDTO.setLikes(0);
        postDTO.setViews(0);

        try {
            postService.createPost(postDTO, imageFile);
            return ResponseEntity.ok("추가 성공");
        } catch (IOException e){
            return ResponseEntity.status(500).body("추가 실패");
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("Authorization") String token) {

        System.out.println("게시글 수정 시도");
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);

        try {
            postService.updatePost(id, userId, title, content, imageFile);
            return ResponseEntity.ok("수정 성공");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("수정 실패");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body("수정 권한 없음");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id,@RequestHeader("Authorization") String token) {
        System.out.println("삭제 시도");
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);
        try{
            postService.deletePost(id,userId);
            System.out.println("삭제 성공");
            return ResponseEntity.noContent().build();
        }
        catch(IllegalArgumentException e){
            System.out.println("삭제 실패-권한없음");
            return ResponseEntity.status(403).body("삭제 권한 없음");
        }

    }
}
