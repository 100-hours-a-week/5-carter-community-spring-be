package com.example.community.repository;

import com.example.community.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시물의 모든 댓글을 찾습니다.
     *
     * @param postId 게시물 ID
     * @return 댓글 리스트
     */
    List<Comment> findByPostId(Long postId);

    /**
     * 특정 사용자의 모든 댓글을 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    void deleteCommentsByUserId(Long userId);

    /**
     * 특정 게시물의 모든 댓글을 삭제합니다.
     *
     * @param postId 게시물 ID
     */
    @Transactional
    void deleteByPostId(Long postId);

    /**
     * 특정 게시물의 댓글 수를 셉니다.
     *
     * @param postId 게시물 ID
     * @return 댓글 수
     */
    long countByPostId(Long postId);
}