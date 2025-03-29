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
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
public class Post {

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
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
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
    
    // 이미지 파일 경로 목록으로 변환
    public List<String> getImageFilesList() {
        if (imageFiles == null || imageFiles.isEmpty()) {
            System.out.println("getImageFilesList: 이미지 파일 없음");
            return new ArrayList<>();
        }
        
        try {
            System.out.println("getImageFilesList: 원본 이미지 경로 = " + imageFiles);
            
            List<String> paths = new ArrayList<>();
            String[] imagePathsArray = imageFiles.split(",");
            
            System.out.println("getImageFilesList: 분리된 이미지 경로 개수 = " + imagePathsArray.length);
            
            for (String path : imagePathsArray) {
                if (path != null && !path.trim().isEmpty()) {
                    // 경로에서 파일명만 추출 (만약 전체 경로가 저장되어 있다면)
                    String fileName = path.trim();
                    if (fileName.contains("/")) {
                        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                    }
                    paths.add(fileName);
                    System.out.println("getImageFilesList: 추가된 이미지 파일명 = " + fileName);
                }
            }
            
            System.out.println("getImageFilesList: 총 " + paths.size() + "개의 이미지 경로 반환");
            return paths;
        } catch (Exception e) {
            System.err.println("이미지 파일 경로 변환 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
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
} 