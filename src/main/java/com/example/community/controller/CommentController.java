package com.example.community.controller;

import com.example.community.model.Comment;
import com.example.community.service.CommentService;
import com.example.community.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/post/{postId}")
    public List<Comment> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestParam("postId") Long postId,
            @RequestParam("content") String content,
            @CookieValue("jwt") String token) {

        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);

        Comment comment = commentService.addComment(postId, userId, content);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long commentId,
            @RequestParam("content") String content,
            @CookieValue("jwt") String token) {

        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);

        Comment updatedComment = commentService.updateComment(commentId, userId, content);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @CookieValue("jwt") String token) {

        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(jwtToken);

        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
