package com.community.site.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bing_picture_comments")
public class BingPictureComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bing_picture_id", nullable = false)
    private BingPicture bingPicture;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BingPictureComment parentComment;
    
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BingPictureComment> replies = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private boolean deleted = false;
    
    // 기본 생성자
    public BingPictureComment() {
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public BingPicture getBingPicture() {
        return bingPicture;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public String getContent() {
        return content;
    }
    
    public BingPictureComment getParentComment() {
        return parentComment;
    }
    
    public List<BingPictureComment> getReplies() {
        return replies;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setBingPicture(BingPicture bingPicture) {
        this.bingPicture = bingPicture;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setParentComment(BingPictureComment parentComment) {
        this.parentComment = parentComment;
    }
    
    public void setReplies(List<BingPictureComment> replies) {
        this.replies = replies;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    // 댓글 생성 편의 메서드
    public static BingPictureComment createComment(BingPicture bingPicture, User author, String content) {
        BingPictureComment comment = new BingPictureComment();
        comment.setBingPicture(bingPicture);
        comment.setAuthor(author);
        comment.setContent(content);
        return comment;
    }
    
    // 답글 생성 편의 메서드
    public static BingPictureComment createReply(BingPicture bingPicture, User author, 
                                                String content, BingPictureComment parentComment) {
        BingPictureComment reply = createComment(bingPicture, author, content);
        reply.setParentComment(parentComment);
        return reply;
    }
    
    // 답글 추가 메서드
    public void addReply(BingPictureComment reply) {
        this.replies.add(reply);
        reply.setParentComment(this);
    }
    
    // 삭제 처리 메서드 (실제 삭제 대신 소프트 삭제)
    public void markAsDeleted() {
        this.deleted = true;
        this.content = "삭제된 댓글입니다.";
    }
} 