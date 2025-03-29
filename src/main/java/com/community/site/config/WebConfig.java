package com.community.site.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Java 8 날짜/시간 포맷터 등록
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        registrar.registerFormatters(registry);
        
        // Java 7 이하 날짜/시간 포맷터 등록
        DateFormatterRegistrar oldRegistrar = new DateFormatterRegistrar();
        oldRegistrar.setFormatter(new DateFormatter("yyyy-MM-dd"));
        oldRegistrar.registerFormatters(registry);
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREA); // 한국어로 기본 로케일 설정
        localeResolver.setDefaultTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Seoul"))); // 한국 시간대 설정
        return localeResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("정적 리소스 핸들러 설정 시작");
        
        // 기본 정적 리소스
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
        
        // 업로드 디렉토리 설정
        String uploadDir = "src/main/resources/static/uploads";
        File uploadDirectory = new File(uploadDir);
        
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
            log.info("업로드 디렉토리 생성: {}", uploadDirectory.getAbsolutePath());
        }
        
        log.info("업로드 디렉토리 경로: {}", uploadDirectory.getAbsolutePath());
        
        // 업로드 디렉토리 내 파일 목록 확인
        if (uploadDirectory.exists() && uploadDirectory.isDirectory()) {
            File[] files = uploadDirectory.listFiles();
            if (files != null) {
                log.info("현재 업로드 디렉토리 파일 {}개:", files.length);
                for (File file : files) {
                    log.info(" - {}: {} bytes", file.getName(), file.length());
                }
            } else {
                log.info("업로드 디렉토리에 파일이 없거나 접근할 수 없습니다.");
            }
        }
        
        // uploads 디렉토리에 대한 리소스 핸들러 명시적으로 추가
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/")
                .setCachePeriod(0);
        
        // 파일 시스템 경로를 통한 접근도 제공 (개발 환경용)
        try {
            String absolutePath = uploadDirectory.getAbsolutePath();
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + absolutePath + "/")
                    .setCachePeriod(0);
            log.info("파일 시스템 리소스 핸들러 추가: file:{}/", absolutePath);
        } catch (Exception e) {
            log.error("파일 시스템 리소스 핸들러 설정 오류: {}", e.getMessage());
        }
        
        log.info("정적 리소스 핸들러 설정 완료");
    }
} 