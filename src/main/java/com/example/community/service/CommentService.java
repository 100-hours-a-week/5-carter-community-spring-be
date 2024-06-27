package com.example.community.service;

import com.example.community.model.Comment;
import com.example.community.model.Post;
import com.example.community.model.User;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 특정 게시물의 모든 댓글을 가져옵니다.
     *
     * @param postId 게시물 ID
     * @return 댓글 리스트
     */
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    /**
     * 새로운 댓글을 추가합니다.
     *
     * @param postId  게시물 ID
     * @param userId  사용자 ID
     * @param content 댓글 내용
     * @return 저장된 댓글
     */
    public Comment addComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .date(LocalDateTime.now())
                .build();

        return commentRepository.save(comment);
    }

    /**
     * 댓글을 수정합니다.
     *
     * @param commentId 댓글 ID
     * @param userId    사용자 ID
     * @param content   댓글 내용
     * @return 수정된 댓글
     */
    public Comment updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한 없음");
        }

        comment.setContent(content);
        comment.setDate(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    /**
     * 댓글을 삭제합니다.
     *
     * @param commentId 댓글 ID
     * @param userId    사용자 ID
     */
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한 없음");
        }

        commentRepository.deleteById(commentId);
    }
}
