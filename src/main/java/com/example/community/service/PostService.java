package com.example.community.service;

import com.example.community.dto.PostDTO;
import com.example.community.model.Post;
import com.example.community.model.User;
import com.example.community.repository.PostRepository;
import com.example.community.repository.CommentRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

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

@RequiredArgsConstructor
@Service
public class PostService {
    private static final String POST_IMAGE_DIRECTORY = "src/main/resources/images/post/";

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    /**
     * 모든 게시물을 가져옵니다.
     *
     * @return 게시물 리스트
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 게시물 ID로 게시물을 가져옵니다.
     *
     * @param id 게시물 ID
     * @return 게시물
     */
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
    }

    /**
     * 새로운 게시물을 생성합니다.
     *
     * @param postDTO 게시물 DTO
     * @param imageFile 이미지 파일
     * @return 생성된 게시물
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public Post createPost(PostDTO postDTO, MultipartFile imageFile) throws IOException {
        User user = userService.getUserById(postDTO.getUserId());

        String imageName = "null.png";
        if (imageFile != null && !imageFile.isEmpty()) {
            imageName = UUID.randomUUID().toString() + "." + getFileExtension(imageFile.getOriginalFilename());
            Path imagePath = Paths.get(POST_IMAGE_DIRECTORY + imageName);
            Files.write(imagePath, imageFile.getBytes());
        }

        Post post = Post.builder()
                .userId(user.getUserId())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .date(postDTO.getDate())
                .likes(postDTO.getLikes())
                .views(postDTO.getViews())
                .image(imageName)
                .build();

        return postRepository.save(post);
    }

    /**
     * 게시물을 업데이트합니다.
     *
     * @param id 게시물 ID
     * @param userId 사용자 ID
     * @param title 제목
     * @param content 내용
     * @param imageFile 이미지 파일
     * @return 업데이트된 게시물
     * @throws IOException 파일 처리 중 오류 발생 시
     */
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

    /**
     * 게시물을 삭제합니다.
     *
     * @param id 게시물 ID
     * @param userId 사용자 ID
     * @throws IllegalArgumentException 유효하지 않은 게시물 ID 또는 삭제 권한 없음
     */
    @Transactional
    public void deletePost(Long id, Long userId) throws IllegalArgumentException{
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        User user = userService.getUserById(userId);
        if (!post.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("수정 권한 없음");
        }
        commentRepository.deleteByPostId(id); // 댓글 삭제
        postRepository.deleteById(id);
    }

    /**
     * 게시물의 조회수를 증가시킵니다.
     *
     * @param id 게시물 ID
     * @return 업데이트된 게시물
     */
    @Transactional
    public Post incrementView(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        post.setViews(post.getViews() + 1);
        return post;
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
     * 게시물의 이미지 파일을 로드합니다.
     *
     * @param postId 게시물 ID
     * @return 이미지 파일 리소스
     * @throws MalformedURLException 유효하지 않은 URL
     */
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

    /**
     * 게시물의 댓글 수를 가져옵니다.
     *
     * @param postId 게시물 ID
     * @return 댓글 수
     */
    public long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

}
