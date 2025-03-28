package com.community.site.config;

import com.community.site.model.User;
import com.community.site.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Bean
    public CommandLineRunner initializeAdmin() {
        return args -> {
            // 기존 관리자가 있는지 확인
            if (userRepository.findByRole("ROLE_ADMIN").isEmpty()) {
                System.out.println("관리자 계정이 없습니다. 기본 관리자 계정을 생성합니다...");
                
                // 관리자 계정 생성
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin1234")); // 초기 비밀번호 설정
                admin.setName("관리자");
                admin.setNickname("관리자");
                admin.setEmail("admin@bigpool.com");
                admin.setRole("ROLE_ADMIN");
                
                userRepository.save(admin);
                
                System.out.println("관리자 계정이 생성되었습니다.");
                System.out.println("아이디: admin");
                System.out.println("비밀번호: admin1234");
                System.out.println("보안을 위해 로그인 후 비밀번호를 변경해 주세요.");
            }
        };
    }
} 