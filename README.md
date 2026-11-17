# POSTpractice2

글·댓글 게시판 REST API를 **아주 작은 단계**로 나눠 만들면서, 단계마다 코드 + 테스트 + 결과 캡처를 커밋하는 실습입니다.

- 참고: [slog.gg/p/14172](https://www.slog.gg/p/14172/raw)
- 기술: Java 25, Spring Boot 4.1.2, Spring Data JPA, H2, Lombok, JUnit 5 + MockMvc
- 테스트 실행: `./gradlew test` (리포트: `build/reports/tests/test/index.html`)

## 진행 상황

| 단계 | 내용 | 상태 |
|---|---|---|
| 1-1 | 빈 Spring Boot 프로젝트 + 앱 실행 테스트 | ✅ 완료 |
| 1-2 | JPA·H2 설정 + `BaseEntity` + `Post` 엔티티 | ⏳ 예정 |
| 1-3 | `PostRepository` + `PostService` + 테스트 | ⏳ 예정 |
| 1-4 | 샘플 글 3건 자동 생성 + 테스트 | ⏳ 예정 |
| 1-5 | `PostComment` 엔티티 + `post.addComment()` | ⏳ 예정 |
| 1-6 | 샘플 댓글 5건 자동 생성 + 테스트 | ⏳ 예정 |
| 2-1 ~ 2-3 | 글 작성 API → 성공 테스트 → 입력값 검증·실패 테스트 | ⏳ 예정 |
| 3-1 ~ 3-3 | 글 삭제 API → 성공 테스트 → 없는 글 삭제·실패 테스트 | ⏳ 예정 |
| 4-1 ~ 4-3 | 댓글 작성 API → 성공 테스트 → 입력값 검증·실패 테스트 | ⏳ 예정 |
| 5-1 ~ 5-3 | 댓글 삭제 API → 성공 테스트 → 없는 댓글 삭제·실패 테스트 | ⏳ 예정 |

## 만들 API

| 메서드 | URL | 기능 | 단계 |
|---|---|---|---|
| POST | `/api/v1/posts` | 글 작성 | 2단계 |
| DELETE | `/api/v1/posts/{id}` | 글 삭제 | 3단계 |
| POST | `/api/v1/posts/{postId}/comments` | 댓글 작성 | 4단계 |
| DELETE | `/api/v1/posts/{postId}/comments/{id}` | 댓글 삭제 | 5단계 |

---

## 1-1: 빈 Spring Boot 프로젝트 + 앱 실행 테스트

### 추가된 파일

| 파일 | 역할 |
|---|---|
| `build.gradle.kts` | 사용할 라이브러리 목록. 지금은 웹(`webmvc`), Lombok, DevTools, 테스트용 라이브러리만 |
| `settings.gradle.kts` | 프로젝트 이름 `POSTpractice2` |
| `gradlew`, `gradle/wrapper/` | Gradle을 따로 설치하지 않아도 빌드할 수 있게 해 주는 래퍼 |
| `src/main/resources/application.yaml` | 앱 설정. 지금은 앱 이름만 |
| `src/main/java/com/back/BackApplication.java` | 앱의 시작점(`main` 메서드) |
| `src/test/java/com/back/BackApplicationTests.java` | 앱이 에러 없이 뜨는지 확인하는 테스트 |

### 핵심 포인트
- `@SpringBootApplication` 이 붙은 클래스의 패키지(`com.back`) **아래에** 있는 클래스만 스프링이 찾아낸다. 그래서 앞으로 만들 모든 클래스는 `com.back.…` 안에 둔다.
- `@SpringBootTest` 는 테스트할 때 스프링을 실제 실행처럼 통째로 띄운다. 테스트 메서드가 비어 있어도, 띄우는 도중에 에러가 나면 실패한다.

### 테스트 결과

| 테스트 | 검증 내용 | 결과 |
|---|---|---|
| `contextLoads` | 앱(스프링)이 에러 없이 실행된다 | ✅ 통과 |

![1-1 테스트 결과](docs/images/step1-1-test.png)
