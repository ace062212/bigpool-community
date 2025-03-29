package com.community.site.service;

import com.community.site.model.Post;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PostService {
    Post createPost(Post post);
    Optional<Post> findById(Long id);
    Page<Post> findAllPosts(Pageable pageable);
    Page<Post> findByAuthor(User author, Pageable pageable);
    Page<Post> searchPosts(String keyword, Pageable pageable);
    Post updatePost(Post post);
    void deletePost(Long id);
    Post incrementViewCount(Long id);
    
    // 비동기 조회 메서드
    CompletableFuture<Page<Post>> findAllPostsAsync(Pageable pageable);
    CompletableFuture<Page<Post>> searchPostsAsync(String keyword, Pageable pageable);
} 