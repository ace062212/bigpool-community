package com.community.site.service;

import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);
    private final PostRepository postRepository;
    
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post createPost(Post post) {
        logger.debug("게시글 생성: {}", post.getTitle());
        return postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#id")
    public Optional<Post> findById(Long id) {
        logger.debug("게시글 조회 by ID: {}", id);
        return postRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "'all-page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Post> findAllPosts(Pageable pageable) {
        logger.debug("모든 게시글 조회: 페이지={}, 크기={}", pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> findByAuthor(User author, Pageable pageable) {
        logger.debug("작성자별 게시글 조회: 사용자ID={}", author.getId());
        return postRepository.findByAuthor(author, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        logger.debug("게시글 검색: 키워드={}", keyword);
        return postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    @Override
    @Transactional
    @CachePut(value = "posts", key = "#post.id")
    @CacheEvict(value = "posts", key = "'all-page-*'")
    public Post updatePost(Post post) {
        logger.debug("게시글 업데이트: ID={}", post.getId());
        return postRepository.save(post);
    }

    @Override
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void deletePost(Long id) {
        logger.debug("게시글 삭제: ID={}", id);
        postRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    @CachePut(value = "posts", key = "#id")
    public Post incrementViewCount(Long id) {
        logger.debug("게시글 조회수 증가: ID={}", id);
        
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setViewCount(post.getViewCount() + 1);
            return postRepository.save(post);
        }
        return null;
    }
    
    /**
     * 비동기로 모든 게시글을 조회합니다.
     */
    @Async("asyncExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<Post>> findAllPostsAsync(Pageable pageable) {
        logger.debug("비동기로 모든 게시글 조회: 페이지={}, 크기={}", pageable.getPageNumber(), pageable.getPageSize());
        return CompletableFuture.completedFuture(postRepository.findAll(pageable));
    }
    
    /**
     * 비동기로 키워드로 게시글을 검색합니다.
     */
    @Async("asyncExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<Post>> searchPostsAsync(String keyword, Pageable pageable) {
        logger.debug("비동기로 게시글 검색: 키워드={}", keyword);
        return CompletableFuture.completedFuture(
            postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
        );
    }
} 