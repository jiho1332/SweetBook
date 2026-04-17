# 🐶 MemoryBook - 반려견 추억 포토북 서비스

---

## 1. 서비스 소개

### 📖 한 줄 설명

반려견과의 추억을 사진과 글로 기록하면 자동으로 책으로 제작하고 주문까지 할 수 있는 감성 포토북 서비스

---

### 🎯 타겟 고객

* 반려견을 키우는 사용자
* 반려동물을 떠나보낸 사용자 (추억 기록 / memorial 목적)
* 개인 맞춤형 포토북 제작을 원하는 사용자

---

### 🚀 주요 기능

* 반려견 정보 입력 (이름, 추모일, 대표사진)
* 책 기본 정보 입력 (제목, 부제, 헌정 문구)
* 판형 및 템플릿 선택 (SweetBook API 연동)
* 추억(사진 + 코멘트) 추가
* 자동 페이지 구성 (Memory → Page 변환)
* 책 미리보기 기능
* SweetBook API를 통한 실제 책 생성
* 책 주문 기능 (Orders API 연동)

---

## 2. 실행 방법

### 📦 프로젝트 빌드

```bash
./gradlew build
```

---

### ▶ 서버 실행

```bash
./gradlew bootRun
```

또는
STS / IntelliJ에서 Spring Boot 실행

---

### 🌐 접속 주소

```text
http://localhost:8081/test/book
```

---

## 3. 환경 설정

### 📌 DB 설정

1. MySQL에서 DB 생성

```sql
CREATE DATABASE sweetbook_db;
```

2. 아래 SQL 파일 실행

* `sql/01_schema.sql`
* `sql/02_data.sql`

※ 기본 데이터가 있어야 정상 동작합니다.

---

### 📌 application.properties 설정

```properties
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/sweetbook_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=본인계정
spring.datasource.password=비밀번호

mybatis.mapper-locations=classpath:/mapper/*.xml
mybatis.type-aliases-package=com.sweetbook.vo

spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp

sweetbook.api.base-url=https://api.sweetbook.com/v1
sweetbook.api.key=발급받은_API_KEY
```

⚠️ API Key는 GitHub에 업로드하지 않습니다.

---

## 4. 사용한 API 목록

| API                                | 용도        |
| ---------------------------------- | --------- |
| POST /books                        | 책 생성      |
| POST /books/{bookUid}/cover        | 표지 생성     |
| POST /books/{bookUid}/contents     | 내지 추가     |
| POST /books/{bookUid}/finalization | 책 완성      |
| POST /orders                       | 책 주문      |
| GET /templates                     | 템플릿 목록 조회 |
| GET /book-specs                    | 판형 조회     |

---

## 5. 서비스 흐름

```
반려견 정보 입력
   ↓
책 기본 정보 입력
   ↓
판형 / 템플릿 선택
   ↓
추억 추가 (사진 + 코멘트)
   ↓
미리보기 생성
   ↓
책 생성 (Books API)
   ↓
표지 생성 (Cover API)
   ↓
내지 추가 (Contents API)
   ↓
책 완성 (Finalization API)
   ↓
주문 (Orders API)
```
## 📌 미리보기 이후 책 생성 처리 과정

미리보기 화면에서 사용자가 책 생성을 요청하면  
SweetBook API를 순차적으로 호출하여 실제 책을 생성합니다.

처리 흐름은 다음과 같습니다.

1. 책 생성 (POST /books)  
2. 표지 추가 (POST /books/{bookUid}/cover)  
3. 내지 추가 (POST /books/{bookUid}/contents)  
   - Memory 데이터를 기반으로 반복 처리  
4. 책 완성 (POST /books/{bookUid}/finalization)  
5. 주문 처리 (POST /orders)  

특히 내지 추가 단계에서는 여러 개의 추억 데이터를 반복 처리해야 하므로  
forEach 기반 반복 구조를 활용하여 페이지를 순차적으로 생성하도록 구현하였습니다.

---

## 6. 프로젝트 구조

```
SweetBook-Project
 ├─ src
 │   └─ main
 │       ├─ java/com/sweetbook
 │       │   ├─ controller
 │       │   ├─ service
 │       │   ├─ mapper
 │       │   ├─ vo
 │       │   └─ config
 │       └─ webapp/WEB-INF/views
 ├─ sql
 │   ├─ 01_schema.sql
 │   └─ 02_data.sql
 ├─ build.gradle
 └─ README.md
```

---

## 7. 설계 의도

### 📌 서비스 기획

단순한 포토북 제작이 아니라
반려견과의 추억을 기록하고 감성적으로 책으로 남길 수 있는 서비스를 목표로 설계

---

### 📌 핵심 구조

* Pet / Memory / BookProject 중심 구조
* Memory 데이터를 Page로 변환하여 Book 생성
* SweetBook API와 직접 연동하여 실제 출력 가능한 구조 구현

---

### 📌 기술 선택 이유

* Spring Boot + MyBatis 기반 백엔드 구조
* JSP 기반 빠른 UI 구현
* REST API 구조로 확장 가능

---

### 📌 비즈니스 가능성

* 반려동물 시장 성장
* 감성 콘텐츠 + 실물 상품 결합
* 개인 맞춤형 포토북 서비스

---

### 📌 개선 및 확장 계획

* 템플릿별 파라미터 자동 매핑 구조 개선
* 커버 템플릿 선택 기능 추가
* 페이지 커스터마이징 기능
* 결제 및 주문 UI 개선

### 📌 구현 특징 (미리보기 최적화)

책 미리보기 기능 구현 시, 페이지 수가 많아질 경우(예: 20~30장 이상)
모든 페이지를 한 번에 렌더링하면 성능 저하 및 UI 가독성 문제가 발생할 수 있습니다.

이를 해결하기 위해 서버에서 전달받은 페이지 데이터를 기반으로
JavaScript의 forEach를 활용하여 동적으로 페이지를 생성하는 구조로 구현하였습니다.

또한 모든 페이지를 동시에 출력하는 방식이 아니라,
현재 페이지 기준으로 하나씩 렌더링하는 페이징 방식으로 개선하여 다음과 같은 효과를 얻었습니다.

- 불필요한 DOM 렌더링 감소
- 긴 스크롤 문제 해결
- 책을 넘기는 UX 구현
- 발표 및 시연 시 가독성 향상

이를 통해 데이터 기반의 유연한 페이지 구성과 성능 최적화를 동시에 달성하였습니다.
---

## 8. AI 도구 사용 내역

| AI 도구   | 활용 내용             |
| ------- | ----------------- |
| ChatGPT | API 연동 구조 설계      |
| ChatGPT | SweetBook API 디버깅 |
| ChatGPT | 백엔드 로직 구현         |
| ChatGPT | 서비스 흐름 및 구조 설계    |
| Manus   | CSS 디자인 참고        |

---

## 9. 주의사항

* API Key는 절대 GitHub에 커밋하지 않습니다.
* `.gitignore`에 민감 정보 파일을 포함해야 합니다.
* SweetBook Sandbox 환경에서 테스트를 진행합니다.
* DB 초기 데이터가 없으면 정상 동작하지 않습니다.

---

## 10. 실행 순서 요약

1. DB 생성
2. SQL 실행
3. application.properties 설정
4. 서버 실행
5. `/test/book` 접속

---

## 11. 프로젝트 목적

본 프로젝트는 단순한 포토북 제작 서비스가 아니라
반려견과의 추억을 기록하고 이를 하나의 이야기 형태의 책으로 제작하는 감성 기반 서비스입니다.
