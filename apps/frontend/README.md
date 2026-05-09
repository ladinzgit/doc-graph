# Frontend (미시작)

frontend 시작 시 아래 보류 사항을 결정하고 이 README를 갱신한다 (보류 섹션 제거).

## 확정

- React + TypeScript

## 보류 — 시작 시 결정

backend가 Spring + OpenAPI를 노출하므로, **OpenAPI spec을 client에서 어떻게 활용할지**가 핵심 축이다.

### API client 생성

| 후보 | 동작 | 트레이드오프 |
| --- | --- | --- |
| **`openapi-typescript`** (현재 `packages/api-types`) | OpenAPI 스펙을 파싱해 TypeScript 타입 정의(interface, request/response 타입)만 생성. 런타임 코드 없음. fetch는 사용자가 직접 작성하고 생성된 타입을 import해 명시 적용 | 가장 가벼움. 단 호출 boilerplate를 endpoint마다 매번 작성 |
| **`openapi-fetch`** | `openapi-typescript`가 생성한 타입을 generic 인자로 받는 6KB runtime wrapper. `client.GET("/users/{id}", { params: { path: { id } } })` 형태로 호출하면 path·method·params·response 타입이 자동 추론 | 코드 생성 없이 타입 안전 + boilerplate 제거. 데이터 페칭 통합(TanStack Query 등)은 별도 — 단순 wrapper라 자유롭게 결합 가능 |
| **`orval`** | OpenAPI → 빌드 단계에서 fetch/axios client 함수 + TanStack Query hooks(`useGetUser`) + Zod 스키마 + MSW handler + Faker mock 데이터를 한꺼번에 코드 생성 | hook까지 자동이라 호출 코드량 최소. 단 생성 코드량 많고 도구 컨벤션에 lock-in |

핵심 트레이드오프: **타입만 + 직접 호출** vs **hook까지 자동 생성**. 후자는 boilerplate 절감 크지만 lock-in.

## 작업자 재량

- Mock 서버 (MSW / Prism / orval auto 생성 등) — backend 미구현 흐름에서 frontend 단독 진행용
- Build 도구 (Vite / Next.js / Remix 등)
- 상태 관리 (TanStack Query / Zustand / Jotai 등)
- Styling (TailwindCSS / CSS Modules / Panda 등)
- 폼 (react-hook-form / TanStack Form 등)
- 라우터 (Build 도구가 정하지 않으면 React Router / TanStack Router 등)

## 시작 시 절차

1. 위 보류를 확정하고 이 README 갱신 (보류 섹션 제거, 확정에 추가)
2. `apps/frontend/`에 의존성·디렉토리 구조 setup
3. OpenAPI → typed client 생성 흐름 수립 (현재 진입점: `packages/api-types/src/schema.ts`)
