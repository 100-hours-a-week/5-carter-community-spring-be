package com.example.community.controller;

import com.example.community.dto.CommentDTO;
import com.example.community.model.Comment;
import com.example.community.service.CommentService;
import com.example.community.util.JwtUtil;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    /**
     * 특정 게시물의 댓글 목록을 가져옵니다.
     *
     * @param postId 게시물 ID
     * @return 댓글 목록
     */
    @GetMapping("/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        try {
            log.info("댓글리스트 요청");
            List<Comment> comments = commentService.getCommentsByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("댓글 리스트를 가져오는 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 새로운 댓글을 추가합니다.
     *
     * @param commentDTO 댓글 정보
     * @param token      JWT 토큰
     * @return 상태 코드 200 (성공)
     */
    @PostMapping
    public ResponseEntity<Void> addComment(
            @RequestBody CommentDTO commentDTO,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("댓글 추가 요청");
            String jwtToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(jwtToken);

            commentService.addComment(commentDTO.getPostId(), userId, commentDTO.getContent());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("댓글을 추가하는 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 댓글을 수정합니다.
     *
     * @param commentId      댓글 ID
     * @param commentRequest 수정할 댓글 내용
     * @param token          JWT 토큰
     * @return 상태 코드 200 (성공)
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentDTO commentRequest,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("댓글 수정 요청");
            String jwtToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(jwtToken);

            commentService.updateComment(commentId, userId, commentRequest.getContent());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("댓글을 수정하는 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 댓글을 삭제합니다.
     *
     * @param commentId 댓글 ID
     * @param token     JWT 토큰
     * @return 상태 코드 204 (성공)
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("댓글 삭제 요청");
            String jwtToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(jwtToken);

            commentService.deleteComment(commentId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("댓글을 삭제하는 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
