package com.community.site.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    /**
     * 단일 이미지 파일을 저장합니다.
     * @param file 업로드된 이미지 파일
     * @return 저장된 파일의 경로
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    String saveImage(MultipartFile file) throws IOException;
    
    /**
     * 여러 이미지 파일을 저장합니다.
     * @param files 업로드된 이미지 파일 목록
     * @return 저장된 파일들의 경로 목록
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    List<String> saveImages(List<MultipartFile> files) throws IOException;
    
    /**
     * 여러 이미지 파일을 저장합니다. (배열 버전)
     * @param files 업로드된 이미지 파일 배열
     * @return 저장된 파일들의 경로 목록
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    List<String> saveImages(MultipartFile[] files) throws IOException;
} 