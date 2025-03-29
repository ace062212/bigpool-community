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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileServiceImpl implements FileService {
    
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    private final String uploadDir = "src/main/resources/static/uploads";
    
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            log.info("업로드 디렉토리 생성 완료: {}", uploadDir);
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패: {}", e.getMessage());
        }
    }
    
    @Override
    public List<String> saveImages(List<MultipartFile> files) {
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
    public List<String> saveImages(MultipartFile[] files) {
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
    public String saveImage(MultipartFile file) {
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
            
            // 파일 저장 경로 설정
            Path targetPath = Paths.get(uploadDir, newFileName);
            
            // 파일 저장
            Files.copy(file.getInputStream(), targetPath);
            log.info("파일 저장 완료: {} -> {}", originalFilename, newFileName);
            
            return newFileName;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage(), e);
            return null;
        }
    }
} 