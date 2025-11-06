# Wirebarley 송금 시스템 API 명세서

## Base URL
```
http://localhost:8080
```

## API 목록

### 계좌 관리

#### 1. 계좌 등록
새로운 계좌를 등록합니다.

**Request**
```http
POST /api/accounts
Content-Type: application/json

{
  "accountNumber": "1234567890"
}
```

**Request Fields**
| 필드 | 타입 | 필수 | 설명 | 제약사항 |
|------|------|------|------|----------|
| accountNumber | String | Y | 계좌번호 | 10-20자리 숫자 |

**Response (201 Created)**
```json
{
  "id": 1,
  "accountNumber": "1234567890",
  "balance": 0,
  "createdAt": "2025-01-05T10:00:00"
}
```

**Error Responses**
- `400 Bad Request`: 유효성 검증 실패
  ```json
  {
    "code": "V001",
    "message": "Validation failed",
    "timestamp": "2025-01-05T10:00:00",
    "fieldErrors": [
      {
        "field": "accountNumber",
        "message": "Account number must be 10-20 digits"
      }
    ]
  }
  ```
- `409 Conflict`: 이미 존재하는 계좌번호
  ```json
  {
    "code": "A002",
    "message": "Account already exists",
    "timestamp": "2025-01-05T10:00:00"
  }
  ```

---

#### 2. 계좌 조회
특정 계좌의 정보를 조회합니다.

**Request**
```http
GET /api/accounts/{accountNumber}
```

**Path Parameters**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| accountNumber | String | Y | 계좌번호 |

**Response (200 OK)**
```json
{
  "id": 1,
  "accountNumber": "1234567890",
  "balance": 100000,
  "createdAt": "2025-01-05T10:00:00"
}
```

**Error Responses**
- `404 Not Found`: 계좌를 찾을 수 없음
  ```json
  {
    "code": "A001",
    "message": "Account not found",
    "timestamp": "2025-01-05T10:00:00"
  }
  ```

---

#### 3. 계좌 삭제
특정 계좌를 삭제합니다.

**Request**
```http
DELETE /api/accounts/{accountNumber}
```

**Path Parameters**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| accountNumber | String | Y | 계좌번호 |

**Response (204 No Content)**
```
(본문 없음)
```

**Error Responses**
- `404 Not Found`: 계좌를 찾을 수 없음

---

### 거래 기능

#### 4. 입금
특정 계좌에 금액을 입금합니다.

**Request**
```http
POST /api/transactions/deposit
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "amount": 100000
}
```

**Request Fields**
| 필드 | 타입 | 필수 | 설명 | 제약사항 |
|------|------|------|------|----------|
| accountNumber | String | Y | 계좌번호 | - |
| amount | BigDecimal | Y | 입금 금액 | 0보다 큰 수 |

**Response (200 OK)**
```json
{
  "id": 1,
  "accountNumber": "1234567890",
  "type": "DEPOSIT",
  "amount": 100000,
  "balanceAfter": 100000,
  "targetAccountNumber": null,
  "fee": null,
  "description": "Deposit",
  "createdAt": "2025-01-05T10:00:00"
}
```

**Error Responses**
- `400 Bad Request`: 유효하지 않은 금액
- `404 Not Found`: 계좌를 찾을 수 없음

---

#### 5. 출금
특정 계좌에서 금액을 출금합니다.

**Request**
```http
POST /api/transactions/withdraw
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "amount": 50000
}
```

**Request Fields**
| 필드 | 타입 | 필수 | 설명 | 제약사항 |
|------|------|------|------|----------|
| accountNumber | String | Y | 계좌번호 | - |
| amount | BigDecimal | Y | 출금 금액 | 0보다 큰 수 |

**Response (200 OK)**
```json
{
  "id": 2,
  "accountNumber": "1234567890",
  "type": "WITHDRAWAL",
  "amount": 50000,
  "balanceAfter": 50000,
  "targetAccountNumber": null,
  "fee": null,
  "description": "Withdrawal",
  "createdAt": "2025-01-05T10:05:00"
}
```

**Business Rules**
- 일일 출금 한도: 1,000,000원
- 계좌 잔액이 출금액보다 많아야 함

**Error Responses**
- `400 Bad Request - A003`: 잔액 부족
  ```json
  {
    "code": "A003",
    "message": "Insufficient balance",
    "timestamp": "2025-01-05T10:00:00"
  }
  ```
- `400 Bad Request - L001`: 일일 출금 한도 초과
  ```json
  {
    "code": "L001",
    "message": "Daily withdrawal limit exceeded (max: 1,000,000)",
    "timestamp": "2025-01-05T10:00:00"
  }
  ```

---

#### 6. 이체
출금 계좌에서 입금 계좌로 금액을 이체합니다.

**Request**
```http
POST /api/transactions/transfer
Content-Type: application/json

{
  "sourceAccountNumber": "1234567890",
  "targetAccountNumber": "0987654321",
  "amount": 100000
}
```

**Request Fields**
| 필드 | 타입 | 필수 | 설명 | 제약사항 |
|------|------|------|------|----------|
| sourceAccountNumber | String | Y | 출금 계좌번호 | - |
| targetAccountNumber | String | Y | 입금 계좌번호 | - |
| amount | BigDecimal | Y | 이체 금액 | 0보다 큰 수 |

**Response (200 OK)**
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

**Response Fields**
| 필드 | 타입 | 설명 |
|------|------|------|
| sourceAccountNumber | String | 출금 계좌번호 |
| targetAccountNumber | String | 입금 계좌번호 |
| transferAmount | BigDecimal | 이체 금액 |
| fee | BigDecimal | 수수료 (이체 금액의 1%) |
| totalDeduction | BigDecimal | 총 차감액 (이체 금액 + 수수료) |
| sourceBalanceAfter | BigDecimal | 이체 후 출금 계좌 잔액 |
| targetBalanceAfter | BigDecimal | 이체 후 입금 계좌 잔액 |
| transferredAt | DateTime | 이체 시각 |

**Business Rules**
- 일일 이체 한도: 3,000,000원 (수수료 제외)
- 수수료: 이체 금액의 1% (소수점 둘째자리 반올림)
- 수수료는 출금 계좌에서 별도 차감
- 동일 계좌로는 이체 불가

**Error Responses**
- `400 Bad Request - T002`: 동일 계좌 이체
  ```json
  {
    "code": "T002",
    "message": "Cannot transfer to the same account",
    "timestamp": "2025-01-05T10:00:00"
  }
  ```
- `400 Bad Request - A003`: 잔액 부족
- `400 Bad Request - L002`: 일일 이체 한도 초과
  ```json
  {
    "code": "L002",
    "message": "Daily transfer limit exceeded (max: 3,000,000)",
    "timestamp": "2025-01-05T10:00:00"
  }
  ```
- `404 Not Found`: 출금 또는 입금 계좌를 찾을 수 없음

---

#### 7. 거래내역 조회
특정 계좌의 거래내역을 조회합니다. 최신순으로 정렬되어 반환됩니다.

**Request**
```http
GET /api/transactions/history/{accountNumber}
```

**Path Parameters**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| accountNumber | String | Y | 계좌번호 |

**Response (200 OK)**
```json
[
  {
    "id": 5,
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
    "id": 3,
    "accountNumber": "1234567890",
    "type": "WITHDRAWAL",
    "amount": 50000,
    "balanceAfter": 500000.00,
    "targetAccountNumber": null,
    "fee": null,
    "description": "Withdrawal",
    "createdAt": "2025-01-05T10:05:00"
  },
  {
    "id": 1,
    "accountNumber": "1234567890",
    "type": "DEPOSIT",
    "amount": 550000,
    "balanceAfter": 550000,
    "targetAccountNumber": null,
    "fee": null,
    "description": "Deposit",
    "createdAt": "2025-01-05T10:00:00"
  }
]
```

**거래 유형 (type)**
- `DEPOSIT`: 입금
- `WITHDRAWAL`: 출금
- `TRANSFER_OUT`: 이체 출금 (내 계좌에서 다른 계좌로)
- `TRANSFER_IN`: 이체 입금 (다른 계좌에서 내 계좌로)
- `FEE`: 수수료 (현재는 사용되지 않음, 이체 시 TRANSFER_OUT에 포함)

**Error Responses**
- `404 Not Found`: 계좌를 찾을 수 없음

---

## 공통 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| A001 | 404 | 계좌를 찾을 수 없음 |
| A002 | 409 | 이미 존재하는 계좌 |
| A003 | 400 | 잔액 부족 |
| T001 | 400 | 유효하지 않은 금액 |
| T002 | 400 | 동일 계좌로 이체 불가 |
| L001 | 400 | 일일 출금 한도 초과 (최대 1,000,000원) |
| L002 | 400 | 일일 이체 한도 초과 (최대 3,000,000원) |
| S001 | 409 | 동시성 업데이트 감지 (재시도 필요) |
| V001 | 400 | 유효성 검증 실패 |
| S999 | 500 | 내부 서버 오류 |

## 동시성 처리

이 시스템은 동시성 이슈를 다음과 같이 처리합니다:

1. **비관적 락 (Pessimistic Lock)**: 계좌 잔액 업데이트 시 SELECT FOR UPDATE 사용
2. **낙관적 락 (Optimistic Lock)**: Account 엔티티에 버전 필드 적용
3. **데드락 방지**: 이체 시 계좌번호 순서대로 락 획득

동시성 충돌 발생 시 에러 코드 `S001`이 반환되며, 클라이언트는 재시도해야 합니다.

## 일일 한도 초기화

일일 한도는 매일 자정 (00:00:00)에 자동으로 초기화됩니다. 날짜별로 별도의 레코드가 생성되어 관리됩니다.

## 예제 시나리오

### 시나리오 1: 계좌 생성 후 입금 및 출금

```bash
# 1. 계좌 생성
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"1234567890"}'

# 2. 입금
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"1234567890","amount":500000}'

# 3. 출금
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"1234567890","amount":100000}'

# 4. 거래내역 조회
curl http://localhost:8080/api/transactions/history/1234567890
```

### 시나리오 2: 계좌 간 이체

```bash
# 1. 첫 번째 계좌 생성 및 입금
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"1234567890"}'

curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"1234567890","amount":1000000}'

# 2. 두 번째 계좌 생성
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"0987654321"}'

# 3. 이체 (100,000원 이체 시 수수료 1,000원 발생)
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountNumber":"1234567890",
    "targetAccountNumber":"0987654321",
    "amount":100000
  }'

# 결과:
# - 1234567890 계좌: 899,000원 (1,000,000 - 100,000 - 1,000)
# - 0987654321 계좌: 100,000원
```