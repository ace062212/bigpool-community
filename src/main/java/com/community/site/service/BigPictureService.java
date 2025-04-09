package com.community.site.service;

import com.community.site.model.BigPicture;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BigPictureService {
    BigPicture createBigPicture(BigPicture bigPicture);
    Optional<BigPicture> findById(Long id);
    Page<BigPicture> findAllBigPictures(Pageable pageable);
    Page<BigPicture> findByAuthor(User author, Pageable pageable);
    Page<BigPicture> searchBigPictures(String keyword, Pageable pageable);
    BigPicture updateBigPicture(BigPicture bigPicture);
    void deleteBigPicture(Long id);
    BigPicture incrementViewCount(Long id);
    
    // 비동기 조회 메서드
    CompletableFuture<Page<BigPicture>> findAllBigPicturesAsync(Pageable pageable);
    CompletableFuture<Page<BigPicture>> searchBigPicturesAsync(String keyword, Pageable pageable);
} 