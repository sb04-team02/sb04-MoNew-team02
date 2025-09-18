# TEAM2 - MoNew [![codecov](https://codecov.io/gh/sb04-team02/sb04-MoNew-team02/graph/badge.svg)](https://codecov.io/gh/sb04-team02/sb04-MoNew-team02)

## 링크
<a href="https://www.notion.so/2-2472c93d1bbc801e992fc5a874008bf1">
  <img src="https://github.com/user-attachments/assets/b8d5ff15-4c53-49ea-83d4-97b08af86455" width="30" height="30" valign="middle" />
  MoNew 팀 노션
</a>
<br><br>
<a href="http://43.200.245.129/#/login">
  <img src="https://github.com/user-attachments/assets/3896030e-a5b5-497e-a8d6-9dddeeecffe0" width="30" height="30" valign="middle" />
  배포 링크 (~2025. ) 논의 후 채우기
</a>

## **프로젝트 소개**

- 프로젝트 기간: 2025.09.01 ~ 2025.09.23
- MongoDB 및 PostgreSQL 백업 및 복구 시스템
- 흩어진 뉴스를 한 곳에, 관심 있는 주제만 모아보세요!\
  모뉴(MoNew)는 다양한 뉴스 출처를 통합하여 관심사 기반으로 뉴스를 저장하는 뉴스 통합 관리 플랫폼입니다.\
  관심 있는 주제의 기사가 등록되면 실시간 알림을 받고, 댓글과 좋아요를 통해 다른 사용자와 의견을 나눌 수 있는 소셜 기능도 함께 제공됩니다.
- Spring Batch를 활용한 안정적인 배치 관리
  - 커스텀 매트릭 정의
  - Actuator에서 배치 작업 모니터링 가능
  - 사용 도메인
    - 뉴스 기사: 수집, 백업
    - 알림: 삭제
    - 사용자: 삭제(물리)
- Mongo DB를 통한 조회 최적화
  -  사용자 활동 내역의 과다 조인 해결
  -  역정규화로 조회 성능 최적화
  - 사용 도메인
    - 사용자 활동 내역
- S3 로그 관리
  - 날짜 별 로그 파일 AWS S3 적재

## **기술 스택**

<!--
 - 기본 개발 환경: IntelliJ, Spring Boot(v3.5.5), Java(v17)
- Database: PostgreSQL(v17.5), MongoDB(Atlas), AWS-RDS
- Storage: AWS-S3
- 배포: Docker, GitHub Actions(CI/CD), AWS(AWS-ECR, AWS-ECS, AWS-EC2)
- 추가 스택: Spring Data JPA, Spring Actuator, Spring WebFlux(네이버 API), Jsoup(RSS), Spring Batch, Mockito, micrometer(커스텀 매트릭)  
- 협업 Tool: Git & Github, Discord, Notion
 -->

| Category | Stacks |
| :--- | :--- |
| **Backend** | <img src="https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white"> <img src="https://img.shields.io/badge/SpringBoot-3.3.5-6DB33F?logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring Batch-6DB33F?logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring WebFlux-6DB33F?logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring Actuator-6DB33F?logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Jsoup-FB8C00?logo=java&logoColor=white"> |
| **Database** | <img src="https://img.shields.io/badge/PostgreSQL-17.5-4169E1?logo=postgresql&logoColor=white"> <img src="https://img.shields.io/badge/MongoDB-Atlas-47A248?logo=mongodb&logoColor=white"> <img src="https://img.shields.io/badge/Amazon RDS-527FFF?logo=amazonrds&logoColor=white"> |
| **Deployment & CI/CD** | <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/GitHub Actions-2088FF?logo=githubactions&logoColor=white"> <img src="https://img.shields.io/badge/Amazon EC2-FF9900?logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/Amazon ECS-FF9900?logo=amazon-ecs&logoColor=white"> <img src="https://img.shields.io/badge/Amazon ECR-FF9900?logo=amazon-ecr&logoColor=white"> |
| **Storage** | <img src="https://img.shields.io/badge/Amazon S3-569A31?logo=amazons3&logoColor=white"> |
| **Monitoring** | <img src="https://img.shields.io/badge/Micrometer-1081C2?logo=micrometer&logoColor=white"> |
| **Testing** | <img src="https://img.shields.io/badge/Mockito-8A2BE2?logo=mockito&logoColor=white"> |
| **Collaboration** | <img src="https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white"> <img src="https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white"> <img src="https://img.shields.io/badge/Discord-5865F2?logo=discord&logoColor=white"> <img src="https://img.shields.io/badge/Notion-000000?logo=notion&logoColor=white"> |
| **IDE** | <img src="https://img.shields.io/badge/IntelliJ IDEA-000000?logo=intellijidea&logoColor=white"> |


---
## **팀원 구성**

김민수 (kms_1015@naver.com)\
김이준 (estherleejunkim@gmail.com)\
문은서 (munes6034@gmail.com)\
신은수 (sin9801@naver.com)\
안중원 (anjoongwon517@gmail.com)\
이지현 (devlee1011@gmail.com)

## **팀원별 구현 기능 상세**
// TODO
### **김민수**
관심사 관리
(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **소셜 로그인 API**
    - Google OAuth 2.0을 활용한 소셜 로그인 기능 구현
    - 로그인 후 추가 정보 입력을 위한 RESTful API 엔드포인트 개발


- **회원 추가 정보 입력 API**
    - 회원 유형(관리자, 학생)에 따른 조건부 입력 처리 API 구현

---

### **김이준**

활동 내역 관리 + 뉴스 기사 백업/복구 + CI/CD와 배포

- **사용자 활동 내역 조회 API**
  - 사용자 별 활동 내역을 조회 가능
  - 포함되는 정보
    - 사용자 정보
    - 구독 중인 관심사
    - 최근 작성한 댓글 (최대 10건)
    - 최근 좋아요를 누른 댓글 (최대 10건)
    - 최근 본 뉴스 기사 (최대 10건)
  - Event Listener을 통해 구현, MongoDB Atlas에 저장
  - Docker Compose를 활용해 로컬 테스트 환경 구축


- **뉴스 기사 S3 백업/복구 기능**
    - 백업 
      - 기사 수집 배치 작업에 따른 데이터 유실에 대비해 뉴스 기사 데이터를 백업하는 기능 구현
      - Spring Batch와 Scheduler를 이용, S3에 일일 단위 자동 백업 로직 구현
    - 복구 기능 API
      - S3 백업 파일과 DB를 비교하여 유실된 데이터를 복구하는 API 개발


- **CI/CD 파이프라인과 AWS 배포**
  - CI/CD 파이프라인
    - GitHub Actions으로 테스트/빌드/배포 자동화 파이프라인 구축 (Docker, ECR, ECS)
    - dockerfile을 통해 멀티 스테이지 빌드를 통해 이미지 최적화
  - AWS 배포
    - 필요한 IAM user 생성
    - RDS, ECS, ECR, S3 환경 구축


---

### **문은서**

알림 관리(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **수강생 정보 관리 API**
    - `GET` 요청을 사용하여 학생의 수강 정보를 조회하는 API 엔드포인트 개발
    - 학생 정보의 CRUD 처리 (Spring Data JPA 사용)


- **공용 Button API**
    - 공통으로 사용할 버튼 기능을 처리하는 API 엔드포인트 구현


---

### **신은수**

댓글 관리(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **관리자 API**
    - `@PathVariable`을 사용한 동적 라우팅 기능 구현
    - `PATCH`, `DELETE` 요청을 사용하여 학생 정보를 수정하고 탈퇴하는 API 엔드포인트 개발
- **CRUD 기능**
    - 학생 정보의 CRUD 기능을 제공하는 API 구현 (Spring Data JPA)
- **회원관리 슬라이더**
    - 학생별 정보 목록을 `Carousel` 형식으로 조회하는 API 구현

---

### **안중원**

뉴스 기사 관리(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **학생 시간 정보 관리 API**
    - 학생별 시간 정보를 `GET` 요청을 사용하여 조회하는 API 구현
    - 실시간 접속 현황을 관리하는 API 엔드포인트


- **수정 및 탈퇴 API**
    - `PATCH`, `DELETE` 요청을 사용하여 수강생의 개인정보 수정 및 탈퇴 처리


- **공용 Modal API**
    - 공통 Modal 컴포넌트를 처리하는 API 구현

---
### **이지현**


사용자 관리

- **사용자 생성 API**
  - 이메일, 패스워드, 닉네임을 입력받아 회원가입 가능
  - 이메일 중복 불가능
  - 패스워드 6자~20자, 닉네임 1자~20자 글자수 제한


- **로그인 API**
  - 이메일, 패스워드로 로그인 가능
  - 로그인을 제외한 모든 화면은 로그인한 사용자만 조회 가능


- **사용자 정보 수정 API**
  - 사용자 정보 업데이트 가능
  - 로그인한 사용자만 변경 가능
  - 닉네임만 변경 가능


- **사용자 논리적 삭제 API**
  - deletedAt 필드 업데이트
  - 실제 데이터베이스에서는 삭제되지 않음


- **사용자 물리적 삭제 API**
  - 데이터베이스에서 해당 사용자를 물리적으로 삭제


- **사용자 물리적 삭제 자동 배치 처리**
  - deletedAt 필드 값이 배치 처리 시작 시간 기준 24시간보다 이전이면 자동으로 DB에서 삭제

---

## **프로젝트 회고록**

// TODO
발표자료 pdf
회고록 링크
채우기

---
## **파일 구조**
```

src/main
├── java
│   └── com
│       └── sprint
│           └── team2
│               └── monew
│                   ├── MonewApplication.java
│                   ├── domain
│                   │   ├── article
│                   │   │   ├── batch
│                   │   │   │   ├── config
│                   │   │   │   │   ├── BackupBatchConfig.java
│                   │   │   │   │   └── BatchConfig.java
│                   │   │   │   └── scheduler
│                   │   │   │       ├── NewsBackupBatchScheduler.java
│                   │   │   │       └── NewsBatchScheduler.java
│                   │   │   ├── collect
│                   │   │   │   ├── Collector.java
│                   │   │   │   ├── NaverApiCollector.java
│                   │   │   │   └── RssCollector.java
│                   │   │   ├── controller
│                   │   │   │   └── ArticleController.java
│                   │   │   ├── dto
│                   │   │   │   ├── request
│                   │   │   │   └── response
│                   │   │   │       ├── ArticleDto.java
│                   │   │   │       ├── ArticleRestoreResultDto.java
│                   │   │   │       ├── ArticleViewDto.java
│                   │   │   │       └── CursorPageResponseArticleDto.java
│                   │   │   ├── entity
│                   │   │   │   ├── Article.java
│                   │   │   │   ├── ArticleDirection.java
│                   │   │   │   ├── ArticleOrderBy.java
│                   │   │   │   └── ArticleSource.java
│                   │   │   ├── exception
│                   │   │   │   ├── ArticleCollectFailedException.java
│                   │   │   │   ├── ArticleErrorCode.java
│                   │   │   │   ├── ArticleException.java
│                   │   │   │   ├── ArticleNotFoundException.java
│                   │   │   │   ├── ArticleSaveFailedException.java
│                   │   │   │   ├── InvalidParameterException.java
│                   │   │   │   ├── NaverApiEmptyResponseException.java
│                   │   │   │   ├── NaverApiFailException.java
│                   │   │   │   └── S3FailureException.java
│                   │   │   ├── mapper
│                   │   │   │   ├── ArticleBackupMapper.java
│                   │   │   │   └── ArticleMapper.java
│                   │   │   ├── repository
│                   │   │   │   ├── ArticleRepository.java
│                   │   │   │   └── ArticleRepositoryCustom.java
│                   │   │   └── service
│                   │   │       ├── ArticleService.java
│                   │   │       ├── ArticleStorageService.java
│                   │   │       └── basic
│                   │   │           ├── BasicArticleService.java
│                   │   │           └── BasicArticleStorageService.java
│                   │   ├── base
│                   │   │   ├── BaseEntity.java
│                   │   │   ├── DeletableEntity.java
│                   │   │   └── UpdatableEntity.java
│                   │   ├── comment
│                   │   │   ├── controller
│                   │   │   │   └── CommentController.java
│                   │   │   ├── dto
│                   │   │   │   ├── CommentDto.java
│                   │   │   │   ├── request
│                   │   │   │   │   ├── CommentRegisterRequest.java
│                   │   │   │   │   └── CommentUpdateRequest.java
│                   │   │   │   └── response
│                   │   │   │       ├── CommentActivityDto.java
│                   │   │   │       └── CursorPageResponseCommentDto.java
│                   │   │   ├── entity
│                   │   │   │   ├── Comment.java
│                   │   │   │   └── CommentSortType.java
│                   │   │   ├── exception
│                   │   │   │   ├── CommentContentRequiredException.java
│                   │   │   │   ├── CommentErrorCode.java
│                   │   │   │   ├── CommentException.java
│                   │   │   │   ├── CommentForbiddenException.java
│                   │   │   │   ├── ContentNotFoundException.java
│                   │   │   │   └── InvalidPageSizeException.java
│                   │   │   ├── mapper
│                   │   │   │   └── CommentMapper.java
│                   │   │   ├── repository
│                   │   │   │   └── CommentRepository.java
│                   │   │   └── service
│                   │   │       ├── CommentService.java
│                   │   │       └── basic
│                   │   │           └── BasicCommentService.java
│                   │   ├── interest
│                   │   │   ├── controller
│                   │   │   │   └── InterestController.java
│                   │   │   ├── dto
│                   │   │   │   ├── InterestDto.java
│                   │   │   │   ├── request
│                   │   │   │   │   ├── CursorPageRequestInterestDto.java
│                   │   │   │   │   ├── InterestRegisterRequest.java
│                   │   │   │   │   └── InterestUpdateRequest.java
│                   │   │   │   └── response
│                   │   │   │       ├── CursorPageResponseInterestDto.java
│                   │   │   │       └── InterestQueryDto.java
│                   │   │   ├── entity
│                   │   │   │   └── Interest.java
│                   │   │   ├── exception
│                   │   │   │   ├── InterestAlreadyExistsSimilarityNameException.java
│                   │   │   │   ├── InterestErrorCode.java
│                   │   │   │   ├── InterestException.java
│                   │   │   │   └── InterestNotFoundException.java
│                   │   │   ├── mapper
│                   │   │   │   └── InterestMapper.java
│                   │   │   ├── repository
│                   │   │   │   ├── InterestRepository.java
│                   │   │   │   ├── InterestRepositoryCustom.java
│                   │   │   │   └── InterestRepositoryImpl.java
│                   │   │   └── service
│                   │   │       ├── InterestService.java
│                   │   │       └── basic
│                   │   │           └── BasicInterestService.java
│                   │   ├── like
│                   │   │   ├── controller
│                   │   │   │   └── ReactionController.java
│                   │   │   ├── dto
│                   │   │   │   ├── CommentLikeDto.java
│                   │   │   │   └── response
│                   │   │   │       └── CommentLikeActivityDto.java
│                   │   │   ├── entity
│                   │   │   │   └── Reaction.java
│                   │   │   ├── exception
│                   │   │   │   ├── ReactionAlreadyExistsException.java
│                   │   │   │   ├── ReactionErrorCode.java
│                   │   │   │   └── ReactionNotFoundException.java
│                   │   │   ├── mapper
│                   │   │   │   └── ReactionMapper.java
│                   │   │   ├── repository
│                   │   │   │   └── ReactionRepository.java
│                   │   │   └── service
│                   │   │       ├── ReactionService.java
│                   │   │       └── basic
│                   │   │           └── BasicReactionService.java
│                   │   ├── notification
│                   │   │   ├── batch
│                   │   │   │   ├── config
│                   │   │   │   │   ├── NotificationCleanupJobConfig.java
│                   │   │   │   │   └── NotificationCleanupScheduler.java
│                   │   │   │   └── listener
│                   │   │   │       ├── NotificationCleanupJobListener.java
│                   │   │   │       └── NotificationCleanupStepListener.java
│                   │   │   ├── controller
│                   │   │   │   └── NotificationController.java
│                   │   │   ├── dto
│                   │   │   │   ├── request
│                   │   │   │   │   └── NotificationCreateDto.java
│                   │   │   │   └── response
│                   │   │   │       ├── CursorPageResponseNotificationDto.java
│                   │   │   │       └── NotificationDto.java
│                   │   │   ├── entity
│                   │   │   │   ├── Notification.java
│                   │   │   │   └── ResourceType.java
│                   │   │   ├── event
│                   │   │   │   ├── CommentLikedEvent.java
│                   │   │   │   └── InterestArticleRegisteredEvent.java
│                   │   │   ├── exception
│                   │   │   │   ├── InvalidFormatException.java
│                   │   │   │   ├── NotificationErrorCode.java
│                   │   │   │   ├── NotificationException.java
│                   │   │   │   └── NotificationNotFoundException.java
│                   │   │   ├── mapper
│                   │   │   │   └── NotificationMapper.java
│                   │   │   ├── repository
│                   │   │   │   └── NotificationRepository.java
│                   │   │   └── service
│                   │   │       ├── NotificationService.java
│                   │   │       └── basic
│                   │   │           └── BasicNotificationsService.java
│                   │   ├── subscription
│                   │   │   ├── dto
│                   │   │   │   └── SubscriptionDto.java
│                   │   │   ├── entity
│                   │   │   │   └── Subscription.java
│                   │   │   ├── exception
│                   │   │   │   ├── SubscriptionAlreadyExistsException.java
│                   │   │   │   ├── SubscriptionErrorCode.java
│                   │   │   │   ├── SubscriptionException.java
│                   │   │   │   └── SubscriptionNotFoundException.java
│                   │   │   ├── mapper
│                   │   │   │   └── SubscriptionMapper.java
│                   │   │   └── repository
│                   │   │       └── SubscriptionRepository.java
│                   │   ├── user
│                   │   │   ├── batch
│                   │   │   │   ├── config
│                   │   │   │   │   ├── UserCleanupJobConfig.java
│                   │   │   │   │   └── UserCleanupScheduler.java
│                   │   │   │   └── listener
│                   │   │   │       ├── UserCleanupJobListener.java
│                   │   │   │       └── UserCleanupStepListener.java
│                   │   │   ├── controller
│                   │   │   │   └── UserController.java
│                   │   │   ├── dto
│                   │   │   │   ├── request
│                   │   │   │   │   ├── UserLoginRequest.java
│                   │   │   │   │   ├── UserRegisterRequest.java
│                   │   │   │   │   └── UserUpdateRequest.java
│                   │   │   │   └── response
│                   │   │   │       └── UserDto.java
│                   │   │   ├── entity
│                   │   │   │   └── User.java
│                   │   │   ├── exception
│                   │   │   │   ├── EmailAlreadyExistsException.java
│                   │   │   │   ├── ForbiddenUserAuthorityException.java
│                   │   │   │   ├── LoginFailedException.java
│                   │   │   │   ├── UserErrorCode.java
│                   │   │   │   ├── UserException.java
│                   │   │   │   └── UserNotFoundException.java
│                   │   │   ├── mapper
│                   │   │   │   └── UserMapper.java
│                   │   │   ├── repository
│                   │   │   │   └── UserRepository.java
│                   │   │   └── service
│                   │   │       ├── UserService.java
│                   │   │       └── basic
│                   │   │           └── BasicUserService.java
│                   │   └── userActivity
│                   │       ├── controller
│                   │       │   └── UserActivityController.java
│                   │       ├── dto
│                   │       │   ├── CommentActivityCancelDto.java
│                   │       │   ├── CommentActivityLikeDto.java
│                   │       │   └── response
│                   │       │       └── UserActivityResponseDto.java
│                   │       ├── entity
│                   │       │   └── UserActivity.java
│                   │       ├── events
│                   │       │   ├── articleEvent
│                   │       │   │   ├── ArticleDeleteEvent.java
│                   │       │   │   └── ArticleViewEvent.java
│                   │       │   ├── commentEvent
│                   │       │   │   ├── CommentAddEvent.java
│                   │       │   │   ├── CommentDeleteEvent.java
│                   │       │   │   ├── CommentLikeAddEvent.java
│                   │       │   │   ├── CommentLikeCancelEvent.java
│                   │       │   │   └── CommentUpdateEvent.java
│                   │       │   ├── subscriptionEvent
│                   │       │   │   ├── SubscriptionAddEvent.java
│                   │       │   │   ├── SubscriptionCancelEvent.java
│                   │       │   │   ├── SubscriptionDeleteEvent.java
│                   │       │   │   └── SubscriptionKeywordUpdateEvent.java
│                   │       │   └── userEvent
│                   │       │       ├── UserCreateEvent.java
│                   │       │       ├── UserDeleteEvent.java
│                   │       │       ├── UserLoginEvent.java
│                   │       │       └── UserUpdateEvent.java
│                   │       ├── exception
│                   │       │   ├── UserActivityErrorCode.java
│                   │       │   ├── UserActivityException.java
│                   │       │   └── UserActivityNotFoundException.java
│                   │       ├── listener
│                   │       │   └── UserActivityListener.java
│                   │       ├── mapper
│                   │       │   └── UserActivityMapper.java
│                   │       ├── repository
│                   │       │   ├── UserActivityRepository.java
│                   │       │   └── UserActivityRepositoryCustom.java
│                   │       └── service
│                   │           ├── UserActivityService.java
│                   │           └── basic
│                   │               └── BasicUserActivityService.java
│                   └── global
│                       ├── api
│                       ├── config
│                       │   ├── JpaAuditingConfig.java
│                       │   ├── JsonConfiguration.java
│                       │   ├── MDCLoggingInterceptor.java
│                       │   ├── MongoConfig.java
│                       │   ├── QuerydslConfig.java
│                       │   ├── WebClientConfig.java
│                       │   ├── WebConfig.java
│                       │   └── aws
│                       │       ├── AwsConfig.java
│                       │       └── S3Properties.java
│                       ├── constant
│                       │   └── ErrorCode.java
│                       ├── error
│                       │   ├── BaseErrorCode.java
│                       │   ├── BusinessException.java
│                       │   ├── DomainException.java
│                       │   ├── ErrorResponse.java
│                       │   └── GlobalExceptionHandler.java
│                       └── log
│                           ├── FileWatchService.java
│                           └── S3Uploader.java
└── resources
    ├── application-dev.yml
    ├── application-prod.yml
    ├── application.yml
    ├── logback-spring.xml
    ├── schema.sql
    ├── static
    │   ├── assets
    │   │   ├── index-D30UMZL2.css
    │   │   └── index-xGh-BI3d.js
    │   ├── favicon.ico
    │   └── index.html
    └── templates

```
