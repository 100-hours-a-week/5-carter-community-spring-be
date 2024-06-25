package com.example.community.repository;

import com.example.community.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 기본 CRUD 메소드들은 JpaRepository가 제공
    @Transactional
    List<Post> findByUserId(Long userId);

    @Transactional
    void deletePostsByUserId(Long userId);
}
