package com.example.community.repository;

import com.example.community.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 기본 CRUD 메소드들은 JpaRepository가 제공
}
