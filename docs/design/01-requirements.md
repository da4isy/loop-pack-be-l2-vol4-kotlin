# 01. 요구사항 명세

## 목표

감성 이커머스 서비스의 상품/브랜드/좋아요/주문 도메인 설계를 완료하고, 구현 전 시나리오 기반 검증까지 마친다.

---

## 유저 여정

> 유저가 브랜드별 상품을 둘러보고 -> 마음에 드는 상품에 좋아요를 누르고 -> 여러 상품을 골라 -> 한 번에 주문한다.

**범위 내:** 상품/브랜드 조회, 좋아요 등록/취소, 주문 생성/조회, 어드민 CRUD  
**범위 밖:** 회원가입, 결제(추후 개발), 검색

---

## 유비쿼터스 언어 사전

| 용어 | 의미 | 비고 |
|------|------|------|
| User | 서비스 이용자 (고객) | Member 아님 |
| Brand | 상품을 묶는 브랜드 단위 | |
| Product | 판매 상품 | |
| Stock | 상품 재고 수량 | Inventory 아님 |
| Like | 유저가 상품에 누르는 좋아요 | 찜/북마크 아님 |
| Order | 주문 건 (1회 결제 단위) | |
| OrderItem | 주문 내 개별 상품 항목 | 주문 시점 스냅샷 포함 |
| PaymentClient | 외부 결제 시스템 연동 인터페이스 | 지금은 mock, 나중에 PG 모듈로 교체 |
| Soft Delete | `deletedAt` 필드로 논리 삭제 | 물리 삭제 없음 |

---

## 도메인별 기능 정의

### 1. Brand + Product (기반 데이터)

#### 대고객 API (`/api/v1`)

| 기능 | 메서드 | 엔드포인트 | 비고 |
|------|--------|-----------|------|
| 상품 목록 조회 | GET | `/api/v1/products` | 정렬: latest(기본), price_asc, likes_desc |
| 상품 상세 조회 | GET | `/api/v1/products/{productId}` | 브랜드 정보 포함, 재고 노출 |
| 브랜드 목록 조회 | GET | `/api/v1/brands` | |
| 브랜드 상세 조회 | GET | `/api/v1/brands/{brandId}` | |

#### 어드민 API (`/api-admin/v1`)

| 기능 | 메서드 | 엔드포인트 | 비고 |
|------|--------|-----------|------|
| 브랜드 등록 | POST | `/api-admin/v1/brands` | |
| 브랜드 수정 | PUT | `/api-admin/v1/brands/{brandId}` | |
| 브랜드 삭제 | DELETE | `/api-admin/v1/brands/{brandId}` | 소속 상품 연쇄 soft delete |
| 상품 등록 | POST | `/api-admin/v1/products` | 삭제된 브랜드에 등록 시 404 |
| 상품 수정 | PUT | `/api-admin/v1/products/{productId}` | |
| 상품 삭제 | DELETE | `/api-admin/v1/products/{productId}` | soft delete |

### 2. Like (유저 행동)

| 기능 | 메서드 | 엔드포인트 | 비고 |
|------|--------|-----------|------|
| 좋아요 등록 | POST | `/api/v1/products/{productId}/likes` | 멱등: 이미 있으면 200 OK |
| 좋아요 취소 | DELETE | `/api/v1/products/{productId}/likes` | 멱등: 없어도 200 OK |
| 내 좋아요 목록 | GET | `/api/v1/likes` | 본인 것만 조회 |

### 3. Order (핵심 비즈니스)

#### 대고객 API

| 기능 | 메서드 | 엔드포인트 | 비고 |
|------|--------|-----------|------|
| 주문 생성 | POST | `/api/v1/orders` | 재고 차감 + 스냅샷 저장 |
| 내 주문 목록 | GET | `/api/v1/orders` | orderedAt 기준 필터 |
| 내 주문 상세 | GET | `/api/v1/orders/{orderId}` | OrderItem 포함 |

#### 어드민 API

| 기능 | 메서드 | 엔드포인트 | 비고 |
|------|--------|-----------|------|
| 주문 목록 조회 | GET | `/api-admin/v1/orders` | 전체 유저 대상 |
| 주문 상세 조회 | GET | `/api-admin/v1/orders/{orderId}` | |

---

## 유저 식별

| 대상 | 헤더 | 비고 |
|------|------|------|
| 고객 | `X-Loopers-LoginId` / `X-Loopers-LoginPw` | 인증 로직 없음, 식별만 |
| 어드민 | `X-Loopers-Ldap` | 인증/인가 없음, 식별만 |

---

## Q&A 합의 사항

### 브랜드 + 상품

- **재고 노출**: 고객에게 재고 수량을 노출한다 (쿠팡/무신사 기준).
- **연쇄 삭제**: 브랜드 지우면 소속 상품도 soft delete. BrandService가 ProductService에 위임해서 하나의 @Transactional 안에서 같이 처리.
- **삭제된 브랜드에 상품 등록**: 404 거부.
- **고객/어드민 API 차이**: prefix 분리 (`/api/v1` vs `/api-admin/v1`) + 어드민은 CUD 권한.

### 좋아요

- **멱등성**: POST는 있어도 200, 없어도 200. DELETE도 동일. HTTP 멱등성 시맨틱 준수.
- **동시성 방어**: DB unique constraint `(user_id, product_id)`. DuplicateKeyException 터지면 catch해서 200 반환.
- **likeCount**: 지금은 매번 COUNT 쿼리. 트래픽 늘면 Product.likeCount 반정규화로 전환.
- **좋아요 목록**: 본인 것만 조회.
- **삭제된 상품의 좋아요**: soft delete된 상품이라도 좋아요는 남겨둠. 조회 시 "삭제된 상품입니다" 표시.

### 주문

- **재고 차감 시점**: 주문 생성할 때 바로 차감 (결제 붙이면 보상 트랜잭션으로 전환).
- **차감 방식**: 조건부 UPDATE (`WHERE stock >= qty`). 0 rows이면 재고 부족 에러.
- **부분 실패**: 하나라도 재고 부족이면 전체 주문 롤백. "품절된 상품이 포함되어 있습니다."
- **스냅샷**: OrderItem에 productPrice, productName, brandName을 주문 시점 값으로 복사. 원본이 바뀌거나 삭제돼도 주문 이력은 그대로 남는다.
- **주문 조회 필터**: orderedAt 기준 기간 필터.
- **OrderFacade**: ProductService + PaymentClient + OrderService를 조율하는 Application 레이어 책임.
- **결제**: PaymentClient 인터페이스로 추상화. 지금은 MockPaymentClient가 항상 성공 반환. 나중에 PG 모듈 들어오면 구현체만 교체.
- **결제 실패 시**: @Transactional 롤백으로 재고 자동 복구. 실 PG 연동 시 보상 트랜잭션으로 전환.

---

## 제외 범위

| 항목 | 이유 |
|------|------|
| 회원가입/로그인 | 헤더 식별로 대체 |
| 실제 결제 연동 | MockPaymentClient로 대체 (PG 모듈 제공 시 구현체 교체) |
| 검색 | 목록 조회 + 정렬로 대체 |
| 장바구니 | 요구사항 범위 밖 |
| 알림 | 요구사항 범위 밖 |
| 어드민 인증/인가 | 헤더 식별로 대체 (RBAC(Role-Based Access Control), audit log는 추후) |
