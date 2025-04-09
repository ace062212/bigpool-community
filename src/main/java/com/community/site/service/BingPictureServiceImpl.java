package com.community.site.service;

import com.community.site.model.BingPicture;
import com.community.site.model.User;
import com.community.site.repository.BingPictureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BingPictureServiceImpl implements BingPictureService {

    private static final Logger logger = LoggerFactory.getLogger(BingPictureServiceImpl.class);
    
    private final BingPictureRepository bingPictureRepository;
    private final FirebaseStorageService firebaseStorageService;
    
    @Autowired
    public BingPictureServiceImpl(BingPictureRepository bingPictureRepository, FirebaseStorageService firebaseStorageService) {
        this.bingPictureRepository = bingPictureRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    @Override
    @Transactional
    public BingPicture createBingPicture(BingPicture bingPicture, List<MultipartFile> imageFiles) throws IOException {
        logger.debug("BingPicture 생성 시작: 제목={}", bingPicture.getTitle());
        
        // 이미지 파일이 있으면 Firebase Storage에 업로드
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<MultipartFile> nonEmptyFiles = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    nonEmptyFiles.add(file);
                }
            }
            
            if (!nonEmptyFiles.isEmpty()) {
                List<String> imagePaths = firebaseStorageService.saveImages(nonEmptyFiles);
                if (imagePaths != null && !imagePaths.isEmpty()) {
                    bingPicture.setImageFilesList(imagePaths);
                    logger.debug("이미지 {} 개가 Firebase Storage에 업로드됨", imagePaths.size());
                }
            }
        }
        
        BingPicture savedBingPicture = bingPictureRepository.save(bingPicture);
        logger.info("BingPicture 저장 완료: id={}, 제목={}", savedBingPicture.getId(), savedBingPicture.getTitle());
        return savedBingPicture;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BingPicture> findById(Long id) {
        logger.debug("BingPicture 조회: id={}", id);
        return bingPictureRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BingPicture> findAllBingPictures(Pageable pageable) {
        logger.debug("모든 BingPicture 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return bingPictureRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BingPicture> findByAuthor(User author, Pageable pageable) {
        logger.debug("사용자별 BingPicture 조회: userId={}, page={}, size={}", 
                   author.getId(), pageable.getPageNumber(), pageable.getPageSize());
        return bingPictureRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BingPicture> searchBingPictures(String keyword, Pageable pageable) {
        logger.debug("BingPicture 검색: keyword={}, page={}, size={}", 
                   keyword, pageable.getPageNumber(), pageable.getPageSize());
        return bingPictureRepository.searchByTitleOrContent(keyword, pageable);
    }

    @Override
    @Transactional
    public BingPicture updateBingPicture(BingPicture bingPicture, List<MultipartFile> newImageFiles) throws IOException {
        logger.debug("BingPicture 업데이트 시작: id={}, 제목={}", bingPicture.getId(), bingPicture.getTitle());
        
        // 이미지 파일이 있으면 Firebase Storage에 업로드
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            List<MultipartFile> nonEmptyFiles = new ArrayList<>();
            for (MultipartFile file : newImageFiles) {
                if (file != null && !file.isEmpty()) {
                    nonEmptyFiles.add(file);
                }
            }
            
            if (!nonEmptyFiles.isEmpty()) {
                List<String> newImagePaths = firebaseStorageService.saveImages(nonEmptyFiles);
                
                if (newImagePaths != null && !newImagePaths.isEmpty()) {
                    // 기존 이미지 파일이 있으면 새 파일과 합침
                    if (bingPicture.getImageFiles() != null && !bingPicture.getImageFiles().isEmpty()) {
                        List<String> existingPaths = bingPicture.getImageFilesList();
                        existingPaths.addAll(newImagePaths);
                        bingPicture.setImageFilesList(existingPaths);
                    } else {
                        bingPicture.setImageFilesList(newImagePaths);
                    }
                    
                    logger.debug("새 이미지 {} 개가 Firebase Storage에 업로드됨", newImagePaths.size());
                }
            }
        }
        
        BingPicture updatedBingPicture = bingPictureRepository.save(bingPicture);
        logger.info("BingPicture 업데이트 완료: id={}, 제목={}", updatedBingPicture.getId(), updatedBingPicture.getTitle());
        return updatedBingPicture;
    }

    @Override
    @Transactional
    public void deleteBingPicture(Long id) {
        logger.debug("BingPicture 삭제: id={}", id);
        bingPictureRepository.deleteById(id);
        logger.info("BingPicture 삭제 완료: id={}", id);
    }

    @Override
    @Transactional
    public BingPicture incrementViewCount(Long id) {
        logger.debug("BingPicture 조회수 증가: id={}", id);
        
        BingPicture bingPicture = bingPictureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BingPicture를 찾을 수 없습니다: " + id));
        
        bingPicture.setViewCount(bingPicture.getViewCount() + 1);
        BingPicture updatedBingPicture = bingPictureRepository.save(bingPicture);
        
        logger.debug("BingPicture 조회수 증가 완료: id={}, 조회수={}", id, updatedBingPicture.getViewCount());
        return updatedBingPicture;
    }
} 