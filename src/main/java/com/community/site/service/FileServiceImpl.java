package com.community.site.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;

@Service
@Primary
public class FileServiceImpl implements FileService {
    
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDirProperty;
    
    // 애플리케이션 외부 경로 (서버 재시작 후에도 유지됨)
    private final String externalUploadDir = "uploads";
    
    // 정적 리소스 경로 (개발 환경용)
    private final String staticUploadDir = "src/main/resources/static/uploads";
    
    @PostConstruct
    public void init() {
        try {
            // 외부 디렉토리 생성
            Files.createDirectories(Paths.get(externalUploadDir));
            log.info("외부 업로드 디렉토리 생성 완료: {}", externalUploadDir);
            
            // 정적 리소스 디렉토리 생성 (개발 환경용)
            Files.createDirectories(Paths.get(staticUploadDir));
            log.info("정적 업로드 디렉토리 생성 완료: {}", staticUploadDir);
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패: {}", e.getMessage());
        }
    }
    
    @Override
    public List<String> saveImages(List<MultipartFile> files) throws IOException {
        List<String> fileNames = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            log.info("저장할 파일이 없습니다.");
            return fileNames;
        }
        
        log.info("총 {} 개의 파일을 저장합니다.", files.size());
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.info("빈 파일이 포함되어 있습니다. 건너뜁니다.");
                continue;
            }
            
            String savedFileName = saveImage(file);
            if (savedFileName != null && !savedFileName.isEmpty()) {
                fileNames.add(savedFileName);
                log.info("파일 저장 성공: {}", savedFileName);
            }
        }
        
        return fileNames;
    }
    
    @Override
    public List<String> saveImages(MultipartFile[] files) throws IOException {
        List<String> fileNames = new ArrayList<>();
        
        if (files == null || files.length == 0) {
            log.info("저장할 파일이 없습니다.");
            return fileNames;
        }
        
        List<MultipartFile> fileList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                fileList.add(file);
            }
        }
        
        return saveImages(fileList);
    }
    
    @Override
    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("저장할 파일이 비어있습니다.");
            return null;
        }
        
        try {
            // 원본 파일명에서 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // UUID로 새 파일명 생성 (확장자 유지)
            String newFileName = UUID.randomUUID().toString() + extension;
            
            // 1. 외부 디렉토리에 저장 (서버 재시작 후에도 유지됨)
            Path externalPath = Paths.get(externalUploadDir, newFileName).toAbsolutePath();
            Files.copy(file.getInputStream(), externalPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("외부 경로에 파일 저장 완료: {} -> {}", originalFilename, externalPath);
            
            // 2. 정적 리소스 경로에도 복사 (개발 환경용)
            try {
                Path staticPath = Paths.get(staticUploadDir, newFileName);
                Files.copy(file.getInputStream(), staticPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("정적 경로에 파일 복사 완료: {}", staticPath);
            } catch (IOException e) {
                log.warn("정적 경로에 파일 복사 실패 (무시됨): {}", e.getMessage());
            }
            
            return newFileName;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean deleteImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            log.warn("삭제할 파일명이 비어있습니다.");
            return false;
        }
        
        boolean success = true;
        
        try {
            // 1. 외부 디렉토리에서 삭제
            Path externalPath = Paths.get(externalUploadDir, fileName).toAbsolutePath();
            boolean externalDeleted = Files.deleteIfExists(externalPath);
            
            if (externalDeleted) {
                log.info("외부 경로에서 파일 삭제 완료: {}", externalPath);
            } else {
                log.warn("외부 경로에서 파일 삭제 실패 (파일 없음): {}", externalPath);
                success = false;
            }
            
            // 2. 정적 리소스 경로에서도 삭제
            Path staticPath = Paths.get(staticUploadDir, fileName);
            boolean staticDeleted = Files.deleteIfExists(staticPath);
            
            if (staticDeleted) {
                log.info("정적 경로에서 파일 삭제 완료: {}", staticPath);
            } else {
                log.warn("정적 경로에서 파일 삭제 실패 (파일 없음): {}", staticPath);
            }
            
            return success;
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            return false;
        }
    }
} 