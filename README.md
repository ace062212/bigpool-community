# BIGPOOL - 스프링 부트 커뮤니티 사이트

이 프로젝트는 스프링 부트로 개발한 간단한 게시판 커뮤니티 사이트입니다.

## 주요 기능

- 회원가입 및 로그인 (Spring Security)
- 게시글 작성, 조회, 수정, 삭제
- 댓글 기능
- 프로필 페이지
- 게시글 검색 기능
- 페이지네이션

## 기술 스택

- Spring Boot 3.2.2
- Spring Security
- Spring Data JPA
- Thymeleaf
- H2 Database (파일 모드)
- Bootstrap 5
- Lombok

## 프로젝트 구조

- `model`: 엔티티 클래스 (User, Post, Comment)
- `repository`: 데이터 액세스 계층
- `service`: 비즈니스 로직 계층
- `controller`: 웹 요청 처리
- `security`: 보안 설정
- `resources/templates`: Thymeleaf 템플릿

## 애플리케이션 실행 방법

### 사전 요구사항

- JDK 17 이상
- Maven

### 빌드 및 실행

1. 프로젝트 클론 또는 다운로드
```bash
git clone <repository-url>
cd <project-directory>
```

### Maven으로 프로젝트 빌드
```bash
mvn clean package -DskipTests
```

### 애플리케이션 실행
```bash
java -jar target/site-0.0.1-SNAPSHOT.jar
```
### 서버 닫기 
lsof -i :8080 | grep java
kill -9 "" ("" 안에는 lsof -i :8080 | grep java 실행후에 기입해야함)

### 브라우저에서 접속
```
http://localhost:8080
```

## H2 데이터베이스 접속

애플리케이션이 실행 중일 때 다음 URL로 H2 콘솔에 접속할 수 있습니다:
```
http://localhost:8080/h2-console
```

접속 정보:
- JDBC URL: `jdbc:h2:file:./h2db/community`
- 사용자명: `sa`
- 비밀번호: (비어있음)

## 데이터베이스 설정

애플리케이션은 파일 기반 H2 데이터베이스를 사용합니다. 
데이터는 `./h2db/community.mv.db` 파일에 저장되어 애플리케이션을 재시작해도 데이터가 유지됩니다.

데이터베이스 설정은 `application.properties` 파일에서 확인할 수 있습니다:
```properties
spring.datasource.url=jdbc:h2:file:./h2db/community
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

## 해결한 문제들

1. **템플릿 경로 문제**: 
   - `/user/login` 템플릿 경로와 Security 설정의 불일치 해결
   - 모든 템플릿이 layout 데코레이터를 사용하도록 통일

2. **H2 데이터베이스 설정**: 
   - 메모리 DB에서 파일 DB로 전환하여 데이터 영속성 확보
   - 호환되지 않는 DB 옵션 제거 및 안정적인 설정 적용

3. **API 경로 통일**: 
   - 컨트롤러와 템플릿의 URL 경로 불일치 수정 
   - 일관된 URL 구조 적용

4. **엔티티 관계 설정**: 
   - Post와 Comment 간의 양방향 관계 설정
   - lazy loading 및 cascade 설정 적용

5. **더미 데이터 비활성화**: 
   - 개발용 더미 데이터 생성 코드 비활성화
   - 프로덕션 환경을 위한 준비 



### 해결해야할 문제들 
로그인을 하지 않고 게시물에 접근시 제대로 조회가 안되고 에러가 뜸 (해결을 어케할지 고민) 로그인 안하면 아예 접근 차단? 아니면 그냥 보게할까? 


### 추가할 기능들 
게시물 이미지 첨부 
다른 게시판 생성 ( 공지사항 등 )
프로필 이미지 생성 