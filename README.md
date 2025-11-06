# Wirebarley 송금 시스템

계좌 간 송금 시스템을 구현한 Spring Boot 애플리케이션입니다.

## 기술 스택

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA (Hibernate)**
- **H2 Database** (In-memory)
- **Lombok**
- **Gradle**
- **Docker & Docker Compose**

## 주요 기능

### 1. 계좌 관리
- 계좌 등록 (POST `/api/accounts`)
- 계좌 삭제 (DELETE `/api/accounts/{accountNumber}`)
- 계좌 조회 (GET `/api/accounts/{accountNumber}`)

### 2. 거래 기능
- **입금** (POST `/api/transactions/deposit`)
  - 특정 계좌에 금액 입금

- **출금** (POST `/api/transactions/withdraw`)
  - 특정 계좌에서 금액 출금
  - 일일 출금 한도: 1,000,000원

- **이체** (POST `/api/transactions/transfer`)
  - 계좌 간 금액 이체
  - 수수료: 이체 금액의 1% (소수점 둘째자리 반올림)
  - 일일 이체 한도: 3,000,000원

### 3. 거래내역 조회
- 거래내역 조회 (GET `/api/transactions/history/{accountNumber}`)
  - 최신순으로 정렬하여 반환

## 실행 방법

### Docker Compose 사용

```bash
# 애플리케이션 빌드 및 실행
docker-compose up --build
```

```bash
# 백그라운드 실행
docker-compose up -d --build
```

```bash
# 종료
docker-compose down
```

### 로컬 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

## API 명세

### 계좌 등록
```bash
POST /api/accounts
Content-Type: application/json

{
  "accountNumber": "1234567890"
}
```

**Response**
```json
{
  "id": 1,
  "accountNumber": "1234567890",
  "balance": 0,
  "createdAt": "2025-01-05T10:00:00"
}
```

### 입금
```bash
POST /api/transactions/deposit
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "amount": 100000
}
```

**Response**
```json
{
  "id": 1,
  "accountNumber": "1234567890",
  "type": "DEPOSIT",
  "amount": 100000,
  "balanceAfter": 100000,
  "description": "Deposit",
  "createdAt": "2025-01-05T10:00:00"
}
```

### 출금
```bash
POST /api/transactions/withdraw
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "amount": 50000
}
```

**Response**
```json
{
  "id": 2,
  "accountNumber": "1234567890",
  "type": "WITHDRAWAL",
  "amount": 50000,
  "balanceAfter": 50000,
  "description": "Withdrawal",
  "createdAt": "2025-01-05T10:05:00"
}
```

### 이체
```bash
POST /api/transactions/transfer
Content-Type: application/json

{
  "sourceAccountNumber": "1234567890",
  "targetAccountNumber": "0987654321",
  "amount": 100000
}
```

**Response**
```json
{
  "sourceAccountNumber": "1234567890",
  "targetAccountNumber": "0987654321",
  "transferAmount": 100000,
  "fee": 1000.00,
  "totalDeduction": 101000.00,
  "sourceBalanceAfter": 399000.00,
  "targetBalanceAfter": 100000,
  "transferredAt": "2025-01-05T10:10:00"
}
```

### 거래내역 조회
```bash
GET /api/transactions/history/{accountNumber}
```

**Response**
```json
[
  {
    "id": 3,
    "accountNumber": "1234567890",
    "type": "TRANSFER_OUT",
    "amount": 100000,
    "balanceAfter": 399000.00,
    "targetAccountNumber": "0987654321",
    "fee": 1000.00,
    "description": "Transfer to 0987654321",
    "createdAt": "2025-01-05T10:10:00"
  },
  {
    "id": 2,
    "accountNumber": "1234567890",
    "type": "WITHDRAWAL",
    "amount": 50000,
    "balanceAfter": 50000,
    "description": "Withdrawal",
    "createdAt": "2025-01-05T10:05:00"
  }
]
```

### 계좌 삭제
```bash
DELETE /api/accounts/{accountNumber}
```

**Response**: 204 No Content

## 에러 응답

```json
{
  "code": "A001",
  "message": "Account not found",
  "timestamp": "2025-01-05T10:00:00"
}
```

### 주요 에러 코드
- `A001`: 계좌를 찾을 수 없음
- `A002`: 이미 존재하는 계좌
- `A003`: 잔액 부족
- `T001`: 유효하지 않은 금액
- `T002`: 동일 계좌로 이체 불가
- `L001`: 일일 출금 한도 초과
- `L002`: 일일 이체 한도 초과
- `S001`: 동시성 업데이트 감지
- `V001`: 유효성 검증 실패

## 데이터베이스 설계

### Account (계좌)
- `id`: 기본키
- `account_number`: 계좌번호 (unique)
- `balance`: 잔액
- `version`: 낙관적 락 버전
- `created_at`: 생성일시
- `updated_at`: 수정일시

### Transaction (거래)
- `id`: 기본키
- `account_number`: 계좌번호
- `type`: 거래유형 (DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN, FEE)
- `amount`: 거래금액
- `balance_after`: 거래 후 잔액
- `target_account_number`: 이체 대상 계좌번호
- `fee`: 수수료
- `description`: 설명
- `created_at`: 거래일시

### DailyLimit (일일 한도)
- `id`: 기본키
- `account_number`: 계좌번호
- `transaction_date`: 거래일자
- `withdrawal_amount`: 당일 출금 누적액
- `transfer_amount`: 당일 이체 누적액

## 주요 구현 사항

### 1. 동시성 제어
- **Pessimistic Lock**: 계좌 잔액 업데이트 시 비관적 락 사용
- **Optimistic Lock**: Account 엔티티에 `@Version` 적용
- **Deadlock 방지**: 이체 시 계좌번호 순서대로 락 획득

### 2. 일일 한도 관리
- 출금 한도: 1,000,000원/일
- 이체 한도: 3,000,000원/일
- 날짜별로 한도 추적 및 검증

### 3. 수수료 계산
- 이체 금액의 1% 수수료 부과
- 소수점 둘째자리 반올림 (HALF_UP)
- 수수료는 출금 계좌에서 별도 차감

### 4. 예외 처리
- 비즈니스 로직 예외를 `BusinessException`으로 통합
- `GlobalExceptionHandler`를 통한 일관된 에러 응답
- 유효성 검증 실패 시 상세 필드 에러 정보 제공

### 5. 테스트
- 단위 테스트: Service 레이어 로직 검증
- 컨트롤러 테스트: `@WebMvcTest`를 통한 API 테스트
- 통합 테스트: 실제 DB 연동 시나리오 테스트

## 아키텍처

```
com.wire.wirebarley/
├── controller/      # REST API 컨트롤러
├── service/         # 비즈니스 로직
├── repository/      # JPA 리포지토리
├── domain/          # 엔티티 및 도메인 모델
├── dto/             # 요청/응답 DTO
└── exception/       # 예외 처리
```

### 계층 구조
- **Controller**: HTTP 요청 처리 및 응답 반환
- **Service**: 비즈니스 로직 및 트랜잭션 관리
- **Repository**: 데이터 접근 계층
- **Domain**: 비즈니스 도메인 모델 및 규칙

## 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests AccountServiceTest

# 특정 테스트 메서드 실행
./gradlew test --tests AccountServiceTest.createAccount_Success
```

## H2 Console 접속

애플리케이션 실행 후 브라우저에서 접속:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:wirebarley`
- Username: `sa`
- Password: (비워두기)

## 추가 확장 가능 사항

1. **멀티모듈 아키텍처**: domain, application, infrastructure 모듈 분리
2. **캐싱**: 계좌 조회 성능 향상을 위한 Redis 캐싱
3. **이벤트 기반 아키텍처**: 거래 완료 시 알림 발송 등
4. **API 문서화**: Swagger/OpenAPI 적용
5. **모니터링**: Spring Actuator 활용한 메트릭 수집
6. **보안**: Spring Security를 통한 인증/인가
7. **데이터베이스**: PostgreSQL/MySQL 등 영구 저장소 사용

## 라이센스

이 프로젝트는 Wirebarley 과제 제출용으로 작성되었습니다.