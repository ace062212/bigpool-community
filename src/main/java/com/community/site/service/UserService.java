package com.community.site.service;

import com.community.site.model.User;

import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    /**
     * 사용자명으로 사용자를 찾습니다.
     * @param username 사용자명
     * @return 사용자 Optional
     */
    public Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User updateUser(User user);
    
    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param user 사용자 객체
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @return 성공 여부
     */
    boolean changePassword(User user, String currentPassword, String newPassword);
} 