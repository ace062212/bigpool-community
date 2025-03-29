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
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.VersionResourceResolver;

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
        
        // 기본 정적 리소스 - 캐싱 적용 (1년)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31536000) // 1년 (초 단위)
                .resourceChain(true)
                .addResolver(new VersionResourceResolver()
                        .addContentVersionStrategy("/**"))
                .addTransformer(new CssLinkResourceTransformer());
        
        // JavaScript 및 CSS 파일에 대한 특별 처리
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(31536000)
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(31536000)
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
        
        // 내부 업로드 디렉토리 설정 (애플리케이션 내부)
        String staticUploadDir = "src/main/resources/static/uploads";
        File staticUploadDirectory = new File(staticUploadDir);
        
        if (!staticUploadDirectory.exists()) {
            staticUploadDirectory.mkdirs();
            log.info("내부 업로드 디렉토리 생성: {}", staticUploadDirectory.getAbsolutePath());
        }
        
        log.info("내부 업로드 디렉토리 경로: {}", staticUploadDirectory.getAbsolutePath());
        
        // 외부 업로드 디렉토리 설정 (애플리케이션 외부 - 재시작해도 유지됨)
        String externalUploadDir = "uploads";
        File externalUploadDirectory = new File(externalUploadDir);
        
        if (!externalUploadDirectory.exists()) {
            externalUploadDirectory.mkdirs();
            log.info("외부 업로드 디렉토리 생성: {}", externalUploadDirectory.getAbsolutePath());
        }
        
        log.info("외부 업로드 디렉토리 경로: {}", externalUploadDirectory.getAbsolutePath());
        
        // 파일 목록 로깅
        logDirectoryContents(staticUploadDirectory, "내부");
        logDirectoryContents(externalUploadDirectory, "외부");
        
        // uploads 디렉토리에 대한 리소스 핸들러 명시적으로 추가
        // 1. 내부 정적 리소스 경로 (개발 환경/배포 환경)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/")
                .setCachePeriod(2592000);  // 30일 (초 단위)
        
        // 2. 외부 파일 시스템 경로 (서버 재시작해도 유지됨)
        try {
            String absolutePath = externalUploadDirectory.getAbsolutePath();
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + absolutePath + "/")
                    .setCachePeriod(2592000);  // 30일 (초 단위)
            log.info("외부 파일 시스템 리소스 핸들러 추가: file:{}/", absolutePath);
        } catch (Exception e) {
            log.error("외부 파일 시스템 리소스 핸들러 설정 오류: {}", e.getMessage());
        }
        
        log.info("정적 리소스 핸들러 설정 완료");
    }
    
    // 디렉토리 내용 로깅 유틸리티 메서드
    private void logDirectoryContents(File directory, String dirType) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                log.info("현재 {}용 업로드 디렉토리 파일 {}개:", dirType, files.length);
                for (File file : files) {
                    log.info(" - {}: {} bytes", file.getName(), file.length());
                }
            } else {
                log.info("{}용 업로드 디렉토리에 파일이 없거나 접근할 수 없습니다.", dirType);
            }
        }
    }
} 