package com.example.community.repository;

import com.example.community.model.Post;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 특정 사용자의 모든 게시물을 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    void deletePostsByUserId(Long userId);
}
