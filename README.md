



#### 소개

- 커뮤니티 서비스를 위한 백엔드 서버 프로젝트입니다.
- 인증 - JWT
- 유저기능 - JPA
- 게시판기능 - JPA
- 기술스택 - 자바,스프링,MySQL
- 개발환경 - MacOS, IntelliJ IDEA

#### 프론트엔드

[Frontend Server](https://github.com/100-hours-a-week/5-carter-community-spring-fe)

#### DEMO
  
[![Video Label](http://img.youtube.com/vi/0rVGNgWae5Y/0.jpg)](https://youtu.be/0rVGNgWae5Y)

#### 폴더 구조

```java
community
|
|
|------ CommunityApplication.class
    |
    |
    |-- config  // 설정
    |   | 
    |   |-- SecurityConfig.class // security 설정
    |   |-- Webconfig.class      // CORS 설정
    |
    |
    |-- controller  // 요청을 매핑해 비즈니스로직 요청
    |   |
    |   |-- AuthController.class
    |   |-- CommentController.class
    |   |-- PostController.class
    |   |-- UserController.class 
    |     
    |
    |-- dto  // 데이터 전송 객체
    |   |
    |   |-- CommentDTO.class
    |   |-- PostDTO.class
    |   |-- UserDTO.class
    |
    |
    |-- filter  // filter
    |   |
    |   |-- JwtAuthenticationFilter.class // JWT 인증 관련 필터
    |
    |
    |-- model  // entity
    |   |
    |   |-- Comment.class
    |   |-- Post.class
    |   |-- User.class
    |
    |
    |-- repository  // 데이터베이스와 상호작용
    |   |
    |   |-- CommentRepository.class
    |   |-- PostRepository.class
    |   |-- UserRepository.class
    |
    |
    |-- service  // 요청받은 작업 수행
    |   |
    |   |-- AuthService.class
    |   |-- CommentService.class
    |   |-- PostService.class
    |   |-- UserService.class
    |   
    |
    |-- util  // util
    |   |
    |   |-- JWTUtil.class // JWT 생성, 유효성 검사등의 작업 담당
```

#### 기타

- 빌더패턴 적용
- 생성자 주입
- 각 요청과 진행상황 로그생성
