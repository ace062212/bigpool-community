package com.community.site.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String name;
    
    @Size(max = 50)
    private String nickname;
    
    @Size(max = 255)
    private String profileImage;

    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email;
    
    @NotBlank
    @Size(max = 20)
    private String role = "ROLE_USER"; // 기본값으로 일반 사용자 권한 설정

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // 닉네임이 비어있으면 이름으로 설정
        if (nickname == null || nickname.isEmpty()) {
            nickname = name;
        }
        // 프로필 이미지가 비어있으면 기본 이미지 설정
        if (profileImage == null || profileImage.isEmpty()) {
            profileImage = "/images/default-profile.png";
        }
        // 역할이 비어있으면 기본값 설정
        if (role == null || role.isEmpty()) {
            role = "ROLE_USER";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getProfileImage() {
        return profileImage;
    }

    public String getEmail() {
        return email;
    }
    
    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Admin 권한 체크 유틸리티 메서드
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }
} 