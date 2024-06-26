package com.example.community.repository;

import com.example.community.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    void deleteByPostId(Long postId);

    @Transactional
    void deleteCommentsByUserId(Long userId);

    @Transactional
    void deleteCommentsByPostId(Long postId);

    long countByPostId(Long postId);
}
