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

### ▶ 서버 실행

```bash
./gradlew bootRun
```

또는 STS / IntelliJ에서 Spring Boot 실행

---

### 🌐 접속 주소

```text
http://localhost:8081/test/book
```

---

## 3. 환경 설정

### 📌 application.properties 설정

```properties
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

---

## 6. 프로젝트 구조

```
com.sweetbook
 ├── controller        # API 및 페이지 컨트롤러
 ├── service           # 비즈니스 로직
 ├── mapper            # MyBatis Mapper
 ├── vo                # 데이터 모델
 ├── config            # 설정 클래스
 └── jsp               # 화면 (JSP)
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
* `.gitignore`에 민감 정보 파일 포함
* SweetBook Sandbox 환경에서 테스트 진행
