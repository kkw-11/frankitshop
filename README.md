프랜킷 백엔드 개발 지원자 김기원

# FrankIt Shop API
## 프로젝트 개요
FrankIt Shop API는 온라인 상품 관리 시스템의 백엔드 API 서비스입니다. 이 API는 사용자 인증, 상품 관리, 상품 옵션 관리 기능을 제공합니다.

### 기술 스택
- Language : Java 23
- Framework : Spring Boot 3.4.3
- Database: MySQL
- ORM : Spring Data JPA
- Authentication : Spring Security + JWT(JSON Web Token)
- API Doc: Notion
- Build Tool : Gradle

## 기능 정의 명세서
### 인증
- 이메일/비밀번호 기반 로그인
- JWT 토큰 인증
- 액세스 토큰/리프레시 토큰 기반 인증 흐름

### 상품 관리
- 상품 등록
- 상품 수정
- 상품 삭제
- 상품 목록 조회
- 상품 상세 조회
- 상품 검색
- 페이징 처리된 상품 목록 조회

### 상품 옵션 관리
- 상품별 옵션 등록(상품별 최대 옵션 3개)
- 상품별 옵션 수정
- 상품별 옵션 삭제
- 상품별 옵션 상세 조회
- 상품별 옵션 목록 조회
- 다양한 옵션 타입 지원 (SELECT, INPUT)
- 옵션별 추가 가격 설정

### ERD
![img.png](img.png)

### API 명세서
- https://www.notion.so/kkwdev/API-1a7096fc2e158085b4d5e39961df52b1?pvs=4