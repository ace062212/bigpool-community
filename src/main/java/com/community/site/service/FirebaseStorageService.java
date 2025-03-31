package com.community.site.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service("firebaseStorageService")
public class FirebaseStorageService implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);
    private static final String IMAGES_PATH = "images/";

    @Override
    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("저장할 파일이 비어있습니다.");
            return null;
        }

        try {
            // 파일명 생성
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown.jpg";
            }
            
            // HEIC 형식 체크
            boolean isHeicFormat = originalFilename.toLowerCase().endsWith(".heic");
            if (isHeicFormat) {
                logger.warn("HEIC 파일 형식이 감지되었습니다. 이 형식은 완전히 지원되지 않을 수 있습니다: {}", originalFilename);
                // HEIC 파일을 JPEG로 변환하는 코드가 필요합니다 (현재 미구현)
            }
            
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            String fullPath = IMAGES_PATH + fileName;
            
            // 파일 업로드
            BlobId blobId = BlobId.of(StorageClient.getInstance().bucket().getName(), fullPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            // 파일 저장
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            Blob blob = storage.create(blobInfo, file.getBytes());
            
            logger.info("Firebase Storage에 이미지 업로드 성공: {}", fileName);
            
            // 업로드 확인을 위해 URL 생성 테스트
            String testUrl = getImageUrl(fileName);
            logger.info("업로드된 이미지 URL 테스트: {}", testUrl);
            
            return fileName;  // 파일명만 반환
        } catch (IOException e) {
            logger.error("Firebase Storage에 이미지 업로드 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<String> saveImages(List<MultipartFile> files) throws IOException {
        List<String> fileNames = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            logger.info("저장할 파일이 없습니다.");
            return fileNames;
        }
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            
            String savedFileName = saveImage(file);
            if (savedFileName != null) {
                fileNames.add(savedFileName);
            }
        }
        
        return fileNames;
    }

    @Override
    public List<String> saveImages(MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        
        return saveImages(Arrays.asList(files));
    }
    
    /**
     * 이미지 삭제 메서드
     */
    @Override
    public boolean deleteImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            logger.warn("삭제할 파일명이 비어있습니다.");
            return false;
        }
        
        try {
            String fullPath = IMAGES_PATH + fileName;
            BlobId blobId = BlobId.of(StorageClient.getInstance().bucket().getName(), fullPath);
            boolean deleted = StorageClient.getInstance().bucket().getStorage().delete(blobId);
            
            if (deleted) {
                logger.info("Firebase Storage에서 이미지 삭제 성공: {}", fileName);
            } else {
                logger.warn("Firebase Storage에서 이미지 삭제 실패: {}", fileName);
            }
            
            return deleted;
        } catch (Exception e) {
            logger.error("Firebase Storage에서 이미지 삭제 중 오류: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 이미지 파일명으로부터 Firebase Storage URL 생성
     */
    public String getImageUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            logger.warn("Firebase Storage URL 생성 실패: 파일명이 비어있습니다.");
            return null;
        }
        
        try {
            String fullPath = IMAGES_PATH + fileName;
            logger.info("Firebase Storage URL 생성 중: 경로={}", fullPath);
            
            // Firebase Storage 다운로드 URL 생성 방식
            String bucketName = StorageClient.getInstance().bucket().getName();
            
            // URL 인코딩된 경로로 변환 (슬래시를 %2F로 변경)
            String encodedPath = fullPath.replace("/", "%2F");
            
            // 파이어베이스 다운로드 URL 형식
            String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" 
                    + encodedPath + "?alt=media";
            
            logger.info("Firebase Storage URL 생성 완료: URL={}", downloadUrl);
            return downloadUrl;
        } catch (Exception e) {
            logger.error("Firebase Storage URL 생성 중 오류: {}", e.getMessage(), e);
            return null;
        }
    }
} 