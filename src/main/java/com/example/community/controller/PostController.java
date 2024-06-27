package com.example.community.controller;

import com.example.community.dto.PostDTO;
import com.example.community.model.Post;
import com.example.community.service.PostService;
import com.example.community.util.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.net.MalformedURLException;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final JwtUtil jwtUtil;

    /**
     * 모든 게시물을 가져옵니다.
     *
     * @return 게시물 목록
     */
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        try {
            log.info("모든 게시물 요청");
            List<Post> posts = postService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("모든 게시물을 가져오는 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 특정 ID의 게시물을 가져옵니다.
     *
     * @param id 게시물 ID
     * @return 게시물
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        try {
            log.info("게시물 요청: {}", id);
            Post post = postService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.error("게시물을 가져오는 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 새로운 게시물을 생성합니다.
     *
     * @param title 게시물 제목
     * @param content 게시물 내용
     * @param imageFile 이미지 파일 (선택 사항)
     * @param token JWT 토큰
     * @return 상태 코드 200 (성공)
     */
    @PostMapping
    public ResponseEntity<Void> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("Authorization") String token) {

        log.info("게시글 추가 시도");
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);

        PostDTO postDTO = PostDTO.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .date(LocalDateTime.now())
                .likes(0)
                .views(0)
                .build();

        try {
            postService.createPost(postDTO, imageFile);
            log.info("게시글 추가 성공");
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IOException e) {
            log.error("게시글 추가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    /**
     * 특정 게시물을 수정합니다.
     *
     * @param id 게시물 ID
     * @param title 수정할 제목
     * @param content 수정할 내용
     * @param imageFile 수정할 이미지 파일 (선택 사항)
     * @param token JWT 토큰
     * @return 상태 코드 200 (성공)
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("Authorization") String token) {

        log.info("게시글 수정 시도: {}", id);
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);

        try {
            postService.updatePost(id, userId, title, content, imageFile);
            log.info("게시글 수정 성공: {}", id);
            return ResponseEntity.ok("수정 성공");
        } catch (IOException e) {
            log.error("게시글 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body("수정 실패");
        } catch (IllegalArgumentException e) {
            log.warn("수정 권한 없음: {}", id);
            return ResponseEntity.status(403).body("수정 권한 없음");
        }
    }

    /**
     * 특정 게시물을 삭제합니다.
     *
     * @param id 게시물 ID
     * @param token JWT 토큰
     * @return 상태 코드 204 (성공) 또는 오류 메시지
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        log.info("게시글 삭제 시도: {}", id);
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);
        try {
            postService.deletePost(id, userId);
            log.info("게시글 삭제 성공: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("게시글 삭제 실패 - 권한 없음: {}", id);
            return ResponseEntity.status(403).body("삭제 권한 없음");
        }
    }

    /**
     * 특정 게시물의 조회수를 1 증가시킵니다.
     *
     * @param id 게시물 ID
     * @return 상태 코드 200 (성공)
     */
    @PutMapping("/{id}/increment-view")
    public ResponseEntity<Void> incrementView(@PathVariable Long id) {
        log.info("조회수 증가 시도: {}", id);
        try {
            Post post = postService.incrementView(id);
            log.info("조회수 증가 성공: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("조회수 증가 실패: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 특정 게시물의 이미지를 가져옵니다.
     *
     * @param postId 게시물 ID
     * @return 이미지 리소스
     */
    @GetMapping("/{postId}/image")
    public ResponseEntity<Resource> getPostImage(@PathVariable Long postId) {
        log.info("이미지 요청: {}", postId);
        try {
            Resource imageResource = postService.loadPostImage(postId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageResource.getFilename() + "\"")
                    .body(imageResource);
        } catch (IllegalArgumentException e) {
            log.error("이미지 요청 실패 - 잘못된 요청: {}", postId);
            return ResponseEntity.badRequest().body(null);
        } catch (MalformedURLException e) {
            log.error("이미지 요청 실패 - 서버 오류: {}", postId);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 특정 게시물의 댓글 수를 가져옵니다.
     *
     * @param postId 게시물 ID
     * @return 댓글 수
     */
    @GetMapping("/{postId}/comments/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long postId) {
        log.info("댓글 수 요청: {}", postId);
        try {
            long commentCount = postService.getCommentCount(postId);
            log.info("댓글 수: {}", commentCount);
            return ResponseEntity.ok(commentCount);
        } catch (Exception e) {
            log.error("댓글 수 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

}
