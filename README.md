프론트엔드

https://github.com/100-hours-a-week/5-carter-community-spring-fe

시연영상
[![Video Label](http://img.youtube.com/vi/0rVGNgWae5Y/0.jpg)](https://youtu.be/0rVGNgWae5Y)

소개

- 커뮤니티 서비스를 위한 백엔드 서버 프로젝트입니다.
- 인증 - JWT
- 유저기능 - JPA
- 게시판기능 - JPA
- 기술스택 - 자바,스프링,MySQL
- 개발환경 - MacOS, IntelliJ IDEA

구조

![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/38552da6-340d-42c1-a9a1-b181ff331f03/9c600a6b-7984-4982-b7be-eb3f8a9705f4/Untitled.png)

![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/38552da6-340d-42c1-a9a1-b181ff331f03/581de318-46e2-43e3-ad02-be7839534c83/Untitled.png)

- config : 시큐리티설정, cors설정
- controller : 요청매핑받아서 비즈니스로직 요청
- dto : 데이터 전송 객체
- filter : JWT인증필터
- model : 엔티티
- repository : 데이터베이스와 상호작용
- service : 요청받은 작업 수행
- util : JWT생성, 유효성 검사 등의 작업

기타

- 빌더패턴 적용
- 생성자 주입
- 각 요청과 진행상황 로그생성
