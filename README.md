# TEAM2 - MoNew [![codecov](https://codecov.io/gh/sb04-team02/sb04-MoNew-team02/graph/badge.svg)](https://codecov.io/gh/sb04-team02/sb04-MoNew-team02)

## 목차
1. [링크](#링크)
2. [프로젝트 소개](#프로젝트-소개)
3. [기술 스택](#기술-스택)
4. [프로젝트 실행 가이드](#프로젝트-실행-가이드)
   - [1. 필수 소프트웨어](#1-필수-소프트웨어)
   - [2. 환경 변수 설정](#2-환경-변수-설정)
   - [3. 데이터베이스 설정](#3-데이터베이스-설정)
   - [4. 외부 API](#4-외부-api)
   - [5. AWS](#5-aws)
   - [6. 프로젝트 실행](#6-프로젝트-실행)
   - [7. 배치/백업 기준](#7-배치백업-기준)
5. [파일 구조](#파일-구조)
6. [팀원 구성](#팀원-구성)

---  

## 링크
<a href="https://www.notion.so/2-2472c93d1bbc801e992fc5a874008bf1">
  <img src="https://github.com/user-attachments/assets/b8d5ff15-4c53-49ea-83d4-97b08af86455" width="30" height="30" valign="middle" />
  MoNew 팀 노션
</a><br><br>
<a href="http://43.200.245.129/#/login">
  <img src="https://github.com/user-attachments/assets/3700f539-d6fe-40b7-869b-e5a4c0a01463" width="30" height="30" valign="middle" />
  배포 링크 ( ~25.09.30 / 25.12.18 ~ 16.06.30  )
</a><br><br>
<a href="http://sprint-project-1196140422.ap-northeast-2.elb.amazonaws.com/sb/monew/api/swagger-ui/index.html">
  <img src="https://github.com/user-attachments/assets/3a34ba65-4ba4-4d1b-a170-16b615bf05cb" width="30" height="30" valign="middle" />
  Swagger API
</a><br><br>
<a href="https://github.com/user-attachments/files/22457754/2._Monew_.pdf">
  <img src="https://github.com/user-attachments/assets/77090a76-0e05-45f6-b563-b885592b8321" width="30" height="30" valign="middle" />
  포트폴리오(pdf)
</a><br>

---

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

---

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

## 프로젝트 실행 가이드
### 1. 필수 소프트웨어
- Docker & Docker Compose
- PostgreSQL (v17.5)
- Java 17, Gradle
- 기타: 인터넷 연결 (외부 API 사용)

### 2. 환경 변수 설정
1. 프로젝트 루트에 `.env` 파일 생성
2. 다음을 참고하여 `.env` 파일 채우기
```
# (Production) RDS 환경
RDS_ENDPOINT=
DB_PORT=
DATABASE_NAME=
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

# (Local) PostgreSQL 환경
POSTGRESQL_DATASOURCE_URL=
POSTGRESQL_DATASOURCE_USERNAME=
POSTGRESQL_DATASOURCE_PASSWORD=

# Naver API
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=

# MongoDB URI
MONGODB_URL=
MONGODB_DB=

# Application Configuration
STORAGE_TYPE=s3
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=

# S3
AWS_S3_BUCKET=
AWS_S3_PRESIGNED_URL_EXPIRATION=
```
> ⚠️ `.env` 파일은 민감 정보가 포함되어 있으므로 Git에 커밋하지 마세요.

### 3. 데이터베이스 설정

#### PostgreSQL
1. `pg_bigm` 설치
```bash
git clone https://github.com/pgbigm/pg_bigm.git
cd pg_bigm
make USE_PGXS=1
make USE_PGXS=1 install
```
2. PostgreSQL 콘솔에서 확장 설치
```
CREATE EXTENSION pg_bigm;
```
#### Mongo DB
```
docker compose -f docker-compose.yml up -d
```

### 4. 외부 API
- 뉴스 기사 수집을 위해 네이버 API 사용
- `.env`에 `NAVER_API_CLIENT_ID`, `NAVER_API_CLIENT_SECRET` 입력 필요

### 5. AWS
- 운영환경 배포 및 파일 저장을 위해 AWS S3, EC2, ECS 사용
- .env에 AWS 관련 키 입력 필요

### 6. 프로젝트 실행
#### 1. Gradle 빌드
```bash
./gradlew clean build
```
#### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```
#### 3. 브라우저에서 http://localhost:8080 접속

### 7. 배치/백업 기준
- 배치: 1시간 주기로 사용자가 관심사로 등록한 키워드를 기준으로 배치 실행
- 백업: S3에 일일 단위 자동 백업 실행

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
│                   │   ├── base
│                   │   ├── comment
│                   │   ├── interest
│                   │   ├── like
│                   │   ├── notification
│                   │   ├── subscription
│                   │   ├── user
│                   │   └── userActivity
│                   └── global
│                       ├── api
│                       ├── config
│                       ├── constant
│                       ├── error
│                       └── log
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

--- 

## **팀원 구성** 

| 이름   | 이메일                         | 담당 기능 | 개인 회고록 |
|--------|--------------------------------|-----------|-------------|
| 김민수 | kms_1015@naver.com             | 관심사 관리 | [🦖](https://www.notion.so/codeit/4-2716fd228e8d80d897bcec6123a1c9cb?p=2612c93d1bbc8066864cf2b11b95cb11&pm=s) |
| 김이준 | estherleejunkim@gmail.com      | 활동 내역 관리, 뉴스 기사 백업 및 복구, CI/CD, 배포 | [🦖](https://www.notion.so/codeit/4-2716fd228e8d80d897bcec6123a1c9cb?p=2612c93d1bbc80f08e54f6513fa4036c&pm=s) |
| 문은서 | munes6034@gmail.com            | 알림 관리 | [👾](https://www.notion.so/codeit/4-2716fd228e8d80d897bcec6123a1c9cb?p=2612c93d1bbc80c7b2daeb32750d77a4&pm=s) |
| 신은수 | sin9801@naver.com              | 댓글 관리 | [⚙️](https://www.notion.so/codeit/4-2716fd228e8d80d897bcec6123a1c9cb?p=2612c93d1bbc80f790c0cb57acca7398&pm=s) |
| 안중원 | anjoongwon517@gmail.com        | 팀장, 뉴스 기사 관리 | [😎](https://www.notion.so/2472c93d1bbc80c59344cf7c46116a51?pvs=24) |
| 이지현 | devlee1011@gmail.com           | 사용자 관리 | [⛱️](https://www.notion.so/codeit/4-2716fd228e8d80d897bcec6123a1c9cb?p=2612c93d1bbc805393eff591997532da&pm=s) |

