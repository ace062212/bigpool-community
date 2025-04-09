package com.community.site.entity;

import com.community.site.model.BigPicture;
import com.community.site.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "big_picture_votes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"big_picture_id", "user_id"}))
@Getter
@Setter
public class BigPictureVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "big_picture_id", nullable = false)
    private BigPicture bigPicture;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private boolean isUpvote; // true: 추천, false: 비추천
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public BigPictureVote() {
        // JPA 요구사항에 따른 기본 생성자
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigPictureVote(BigPicture bigPicture, User user, boolean isUpvote) {
        this.bigPicture = bigPicture;
        this.user = user;
        this.isUpvote = isUpvote;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public BigPicture getBigPicture() {
        return bigPicture;
    }
    
    public User getUser() {
        return user;
    }
    
    public boolean isUpvote() {
        return isUpvote;
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
    
    public void setBigPicture(BigPicture bigPicture) {
        this.bigPicture = bigPicture;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setUpvote(boolean upvote) {
        isUpvote = upvote;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 