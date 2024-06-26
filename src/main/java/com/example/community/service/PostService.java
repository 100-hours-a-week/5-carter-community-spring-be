package com.example.community.service;

import com.example.community.dto.PostDTO;
import com.example.community.model.Post;
import com.example.community.model.User;
import com.example.community.repository.PostRepository;
import com.example.community.repository.CommentRepository;
import com.example.community.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {
    private static final String POST_IMAGE_DIRECTORY = "src/main/resources/images/post/";

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
    }

    public Post createPost(PostDTO postDTO, MultipartFile imageFile) throws IOException {
        User user = userService.getUserById(postDTO.getUserId());

        Post post = new Post();
        post.setUserId(user.getUserId());
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setDate(postDTO.getDate());
        post.setLikes(postDTO.getLikes());
        post.setViews(postDTO.getViews());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = UUID.randomUUID().toString() + "." + getFileExtension(imageFile.getOriginalFilename());
            Path imagePath = Paths.get(POST_IMAGE_DIRECTORY + imageName);
            Files.write(imagePath, imageFile.getBytes());
            post.setImage(imageName);
            postDTO.setImage(imageName); // DTO에 이미지 이름 설정
        }
        else{
            String imageName = "null.png";
            post.setImage(imageName);
            postDTO.setImage(imageName);
        }
        System.out.println("추가직전");
        return postRepository.save(post);
    }

    public Post updatePost(Long id, Long userId, String title, String content, MultipartFile imageFile) throws IOException {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        User user = userService.getUserById(userId);

        if (!post.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("수정 권한 없음");
        }

        post.setTitle(title);
        post.setContent(content);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = post.getImage();
            if (imageName.equals("null.png")){
                imageName = UUID.randomUUID().toString() + "." + getFileExtension(imageFile.getOriginalFilename());
            }
            Path imagePath = Paths.get(POST_IMAGE_DIRECTORY + imageName);
            Files.write(imagePath, imageFile.getBytes());
            post.setImage(imageName);
        }
        else{
            String imageName = post.getImage();
            if (!imageName.equals("null.png")){
                Files.deleteIfExists(Paths.get(POST_IMAGE_DIRECTORY + imageName));
            }
            post.setImage("null.png");
        }

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id, Long userId) throws IllegalArgumentException{
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        User user = userService.getUserById(userId);
        if (!post.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("수정 권한 없음");
        }
        commentRepository.deleteByPostId(id); // 댓글 삭제
        postRepository.deleteById(id);
        System.out.println("게시글 삭제 성공");
    }

    @Transactional
    public Post incrementView(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        post.setViews(post.getViews() + 1);
        return post;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public Resource loadPostImage(Long postId) throws MalformedURLException {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isPresent()) {
            String imageName = postOptional.get().getImage();
            Path imagePath = Paths.get(POST_IMAGE_DIRECTORY).resolve(imageName).normalize();
            return new UrlResource(imagePath.toUri());
        } else {
            throw new IllegalArgumentException("Invalid post ID");
        }
    }

    public long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

}
