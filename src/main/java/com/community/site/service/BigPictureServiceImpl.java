package com.community.site.service;

import com.community.site.model.BigPicture;
import com.community.site.model.User;
import com.community.site.repository.BigPictureRepository;
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
public class BigPictureServiceImpl implements BigPictureService {

    private static final Logger logger = LoggerFactory.getLogger(BigPictureServiceImpl.class);
    private final BigPictureRepository bigPictureRepository;
    
    public BigPictureServiceImpl(BigPictureRepository bigPictureRepository) {
        this.bigPictureRepository = bigPictureRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "bigpictures", allEntries = true)
    public BigPicture createBigPicture(BigPicture bigPicture) {
        logger.debug("빅픽처 생성: {}", bigPicture.getTitle());
        return bigPictureRepository.save(bigPicture);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bigpictures", key = "#id")
    public Optional<BigPicture> findById(Long id) {
        logger.debug("빅픽처 조회 by ID: {}", id);
        return bigPictureRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bigpictures", key = "'all-page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BigPicture> findAllBigPictures(Pageable pageable) {
        logger.debug("모든 빅픽처 조회: 페이지={}, 크기={}", pageable.getPageNumber(), pageable.getPageSize());
        return bigPictureRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BigPicture> findByAuthor(User author, Pageable pageable) {
        logger.debug("작성자별 빅픽처 조회: 사용자ID={}", author.getId());
        return bigPictureRepository.findByAuthor(author, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BigPicture> searchBigPictures(String keyword, Pageable pageable) {
        logger.debug("빅픽처 검색: 키워드={}", keyword);
        return bigPictureRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    @Override
    @Transactional
    @CachePut(value = "bigpictures", key = "#bigPicture.id")
    @CacheEvict(value = "bigpictures", key = "'all-page-*'")
    public BigPicture updateBigPicture(BigPicture bigPicture) {
        logger.debug("빅픽처 업데이트: ID={}", bigPicture.getId());
        return bigPictureRepository.save(bigPicture);
    }

    @Override
    @Transactional
    @CacheEvict(value = "bigpictures", allEntries = true)
    public void deleteBigPicture(Long id) {
        logger.debug("빅픽처 삭제: ID={}", id);
        bigPictureRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    @CachePut(value = "bigpictures", key = "#id")
    public BigPicture incrementViewCount(Long id) {
        logger.debug("빅픽처 조회수 증가: ID={}", id);
        
        Optional<BigPicture> optionalBigPicture = bigPictureRepository.findById(id);
        if (optionalBigPicture.isPresent()) {
            BigPicture bigPicture = optionalBigPicture.get();
            bigPicture.setViewCount(bigPicture.getViewCount() + 1);
            return bigPictureRepository.save(bigPicture);
        }
        return null;
    }
    
    /**
     * 비동기로 모든 빅픽처를 조회합니다.
     */
    @Override
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Page<BigPicture>> findAllBigPicturesAsync(Pageable pageable) {
        logger.debug("비동기 모든 빅픽처 조회: 페이지={}, 크기={}", pageable.getPageNumber(), pageable.getPageSize());
        return CompletableFuture.completedFuture(bigPictureRepository.findAll(pageable));
    }
    
    /**
     * 비동기로 키워드로 빅픽처를 검색합니다.
     */
    @Override
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Page<BigPicture>> searchBigPicturesAsync(String keyword, Pageable pageable) {
        logger.debug("비동기 빅픽처 검색: 키워드={}", keyword);
        return CompletableFuture.completedFuture(
            bigPictureRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
        );
    }
} 