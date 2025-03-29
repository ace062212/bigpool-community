package com.community.site.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    
    @Value("${firebase.storage.bucket}")
    private String storageBucket;
    
    @Value("${firebase.credentials.encoded:}")
    private String firebaseCredentialsEncoded;
    
    @Value("${firebase.service-account.path:}")
    private String firebaseServiceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            // 기존 Firebase 앱이 있는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials;
                
                // 1. 먼저 환경 변수에서 인코딩된 자격 증명을 확인
                if (firebaseCredentialsEncoded != null && !firebaseCredentialsEncoded.isEmpty() && 
                    !firebaseCredentialsEncoded.equals("${FIREBASE_CREDENTIALS:}")) {
                    logger.info("환경 변수에서 Firebase 자격 증명을 로드합니다.");
                    byte[] decodedCredentials = Base64.getDecoder().decode(firebaseCredentialsEncoded);
                    credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedCredentials));
                } 
                // 2. 환경 변수가 없으면 지정된 경로에서 파일 로드
                else if (firebaseServiceAccountPath != null && !firebaseServiceAccountPath.isEmpty()) {
                    logger.info("지정된 경로에서 Firebase 자격 증명 파일을 로드합니다: {}", firebaseServiceAccountPath);
                    credentials = GoogleCredentials.fromStream(new FileInputStream(firebaseServiceAccountPath));
                } 
                // 3. 둘 다 없으면 클래스패스에서 기본 파일 로드 시도
                else {
                    logger.info("클래스패스에서 Firebase 자격 증명 파일을 로드합니다.");
                    InputStream serviceAccount = getClass().getClassLoader()
                            .getResourceAsStream("firebase-service-account.json");
                    if (serviceAccount == null) {
                        throw new IOException("클래스패스에서 Firebase 자격 증명 파일을 찾을 수 없습니다.");
                    }
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setStorageBucket(storageBucket)
                        .build();
                
                FirebaseApp.initializeApp(options);
                logger.info("Firebase 애플리케이션이 성공적으로 초기화되었습니다.");
                logger.info("Firebase Storage 버킷: {}", storageBucket);
                
                // 버킷 접근 테스트
                try {
                    Storage storage = StorageOptions.getDefaultInstance().getService();
                    String bucketName = storage.get(storageBucket).getName();
                    logger.info("Firebase Storage 버킷 접근 성공: {}", bucketName);
                } catch (Exception e) {
                    logger.error("Firebase Storage 버킷 접근 실패: {}", e.getMessage(), e);
                }
            } else {
                logger.info("Firebase 애플리케이션이 이미 초기화되어 있습니다.");
            }
        } catch (IOException e) {
            logger.error("Firebase 초기화 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }
} 