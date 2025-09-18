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

- 기본 개발 환경: IntelliJ, Spring Boot(v3.5.5), Java(v17)
- Database: PostgreSQL(v17.5), MongoDB(Atlas), AWS-RDS
- Storage: AWS-S3
- 배포: Docker, GitHub Actions(CI/CD), AWS(AWS-ECR, AWS-ECS, AWS-EC2)
- 추가 스택: Spring Data JPA, Spring Actuator, Spring WebFlux(네이버 API), Jsoup(RSS), Spring Batch, Mockito, micrometer(커스텀 매트릭)  
- 협업 Tool: Git & Github, Discord, Notion

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

### **김이준**

활동 내역 관리 + CI/CD(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **회원별 권한 관리**
    - Spring Security를 활용하여 사용자 역할에 따른 권한 설정
    - 관리자 페이지와 일반 사용자 페이지를 위한 조건부 라우팅 처리
- **반응형 레이아웃 API**
    - 클라이언트에서 요청된 반응형 레이아웃을 위한 RESTful API 엔드포인트 구현

### **문은서**

알림 관리(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **수강생 정보 관리 API**
    - `GET` 요청을 사용하여 학생의 수강 정보를 조회하는 API 엔드포인트 개발
    - 학생 정보의 CRUD 처리 (Spring Data JPA 사용)
- **공용 Button API**
    - 공통으로 사용할 버튼 기능을 처리하는 API 엔드포인트 구현

### **신은수**

댓글 관리(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **댓글 API**
    - 댓글 작성
      - 댓글 입력 창에 댓글을 작성하여 엔터키를 누르면 뉴스기사 하단에 댓글이 등록되는 기능
      - 각 뉴스 기사별로 등록이 가능
    - 댓글 수정
      - 본인이 작성한 댓글만 수정 가능
    - 댓글 삭제
      - 논리 삭제
        - deletedAt 필드를 업데이트하여 댓글 목록에 deletedAt 필드가 없는 댓글만 나타나도록 구현
        - 목록에선 사라지지만 실제 데이터베이스에선 삭제되지 않고 남아있음
        - 이 프로젝트에선 논리 삭제를 기본 원칙으로 함
      - 물리 삭제
        - 데이터 베이스에서 해당 댓글과 관련된 정보(알림, 좋아요 등)를 완전히 삭제
    - 댓글 목록 조회
      - 뉴스 기사 별로 목록 조회 가능
      - 날짜와 좋아요 수 중 1개의 속성을 기준으로 정렬할 수 있음
      - 커서를 내리면 댓글 목록을 불러오도록 커서 페이지네이션으로 목록조회를 구현

- **댓글 좋아요 API**
  - 댓글 좋아요
    - 사용자가 작성한 댓글에 좋아요를 할 수 있음
    - 좋아요를 누를 때, 좋아요가 성공했다는 표시와 함께 카운트가 1 증가
  - 댓글 좋아요 취소
    - 사용자가 작성한 댓글에 좋아요를 취소할 수 있음
    - 좋아요 표시가 원상태로 돌아오며 좋아요 카운트가 1 감소

### **안중원**

뉴스 기사 관리(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **학생 시간 정보 관리 API**
    - 학생별 시간 정보를 `GET` 요청을 사용하여 조회하는 API 구현
    - 실시간 접속 현황을 관리하는 API 엔드포인트
- **수정 및 탈퇴 API**
    - `PATCH`, `DELETE` 요청을 사용하여 수강생의 개인정보 수정 및 탈퇴 처리
- **공용 Modal API**
    - 공통 Modal 컴포넌트를 처리하는 API 구현

### **이지현**

사용자 관리 (자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **학생 시간 정보 관리 API**
    - 학생별 시간 정보를 `GET` 요청을 사용하여 조회하는 API 구현
    - 실시간 접속 현황을 관리하는 API 엔드포인트
- **수정 및 탈퇴 API**
    - `PATCH`, `DELETE` 요청을 사용하여 수강생의 개인정보 수정 및 탈퇴 처리
- **공용 Modal API**
    - 공통 Modal 컴포넌트를 처리하는 API 구현

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
