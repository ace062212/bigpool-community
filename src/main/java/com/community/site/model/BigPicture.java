package com.community.site.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "big_pictures")
@NoArgsConstructor
@AllArgsConstructor
public class BigPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    private int viewCount = 0;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "bigPicture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    // 이미지 파일 경로 (여러 이미지는 쉼표로 구분)
    @Column(columnDefinition = "TEXT")
    private String imageFiles;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public int getViewCount() {
        return viewCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public String getImageFiles() {
        return imageFiles;
    }
    
    // 이미지 파일 리스트 반환 (쉼표로 분리된 문자열을 리스트로 변환)
    public List<String> getImageFilesList() {
        List<String> imageList = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            String[] files = imageFiles.split(",");
            for (String file : files) {
                if (!file.trim().isEmpty()) {
                    imageList.add(file.trim());
                }
            }
        }
        return imageList;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    public void setImageFiles(String imageFiles) {
        this.imageFiles = imageFiles;
    }
    
    // 이미지 파일 추가
    public void addImageFile(String imageFile) {
        if (this.imageFiles == null || this.imageFiles.isEmpty()) {
            this.imageFiles = imageFile;
        } else {
            this.imageFiles += "," + imageFile;
        }
    }
} 