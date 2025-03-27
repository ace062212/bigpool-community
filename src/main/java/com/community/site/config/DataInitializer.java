package com.community.site.config;

import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.repository.PostRepository;
import com.community.site.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    // 더미 데이터 생성 기능 비활성화
    /*
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                     PostRepository postRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            // 샘플 사용자 생성
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setEmail("admin@example.com");
            admin.setName("관리자");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword(passwordEncoder.encode("password"));
            user1.setEmail("user1@example.com");
            user1.setName("사용자1");
            user1.setCreatedAt(LocalDateTime.now());
            userRepository.save(user1);
            
            // 샘플 게시글 생성
            Post post1 = new Post();
            post1.setTitle("BIGPOOL에 오신 것을 환영합니다");
            post1.setContent("이곳은 지식과 경험의 큰 바다, BIGPOOL입니다. 다양한 주제에 대해 자유롭게 의견을 나누고 소통할 수 있는 공간입니다. 많은 참여 부탁드립니다!");
            post1.setAuthor(admin);
            post1.setCreatedAt(LocalDateTime.now());
            post1.setViewCount(15);
            postRepository.save(post1);
            
            Post post2 = new Post();
            post2.setTitle("스프링 부트로 웹 애플리케이션 개발하기");
            post2.setContent("스프링 부트는 스프링 프레임워크를 기반으로 한 웹 애플리케이션 개발 도구입니다. 이 글에서는 스프링 부트를 사용한 웹 애플리케이션 개발 방법에 대해 알아보겠습니다.\n\n" +
                           "1. 스프링 부트 시작하기\n" +
                           "2. 컨트롤러와 모델 만들기\n" +
                           "3. 뷰 템플릿 작성하기\n" +
                           "4. 데이터베이스 연결하기\n" +
                           "5. 보안 설정하기\n\n" +
                           "각 단계별로 자세한 설명과 예제 코드를 제공할 예정입니다.");
            post2.setAuthor(user1);
            post2.setCreatedAt(LocalDateTime.now().minusDays(1));
            post2.setViewCount(42);
            postRepository.save(post2);
            
            Post post3 = new Post();
            post3.setTitle("자바 프로그래밍 기초");
            post3.setContent("자바는 객체지향 프로그래밍 언어로, 다양한 플랫폼에서 실행할 수 있는 특징을 가지고 있습니다. 이 글에서는 자바 프로그래밍의 기초에 대해 알아보겠습니다.\n\n" +
                           "1. 자바 설치 및 환경 설정\n" +
                           "2. 변수와 데이터 타입\n" +
                           "3. 연산자와 제어문\n" +
                           "4. 클래스와 객체\n" +
                           "5. 상속과 다형성\n\n" +
                           "초보자도 쉽게 따라할 수 있는 예제로 설명하겠습니다.");
            post3.setAuthor(admin);
            post3.setCreatedAt(LocalDateTime.now().minusDays(2));
            post3.setViewCount(30);
            postRepository.save(post3);
            
            System.out.println("데이터베이스 초기화 완료: " + 
                              userRepository.count() + "명의 사용자, " + 
                              postRepository.count() + "개의 게시글이 생성되었습니다.");
        };
    }
    */
} 