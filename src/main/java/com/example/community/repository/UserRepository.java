package com.example.community.repository;

import com.example.community.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이메일로 사용자를 찾습니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 객체를 감싸는 Optional 객체
     */
    Optional<User> findByEmail(String email);

    /**
     * 주어진 닉네임이 존재하는지 확인합니다.
     *
     * @param nickname 사용자 닉네임
     * @return 닉네임이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByNickname(String nickname);

    /**
     * 주어진 이메일이 존재하는지 확인합니다.
     *
     * @param email 사용자 이메일
     * @return 이메일이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByEmail(String email);
}
