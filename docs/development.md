# 개발 환경 가이드

## 사전 준비

- Docker Desktop (또는 Docker Engine + Compose)
- Java 21
- Node.js 20+
- Python 3.12+ + [uv](https://docs.astral.sh/uv/getting-started/installation/)
- [just](https://just.systems/) — 모든 dev 명령의 진입점
- [dotenvx](https://dotenvx.com/docs/install) — 환경변수 주입·암호화
- (Windows 전용) Git for Windows — `C:\Program Files\Git\usr\bin`이 PATH에 포함되어야 한다

---

## 환경변수

| 파일 | 추적 | 역할 |
| --- | --- | --- |
| `.env` | commit | 평문 default + 팀 공유 시크릿 (dotenvx 암호화) |
| `.env.local` | gitignored | 개인 시크릿 |
| `.env.keys` | gitignored | dotenvx 복호화 키 |

1. `just setup` — `.env.local` 생성 + JS 의존성 설치
2. `.env.keys` 수령
3. `.env`의 빈 placeholder 항목을 `.env.local`에 채움

---

## 로컬 인프라 구조

`docker-compose.yml` — 기본 인프라

| 컨테이너 | 호스트 포트 | 용도 |
| --- | --- | --- |
| postgres | 5433 | 개발용 DB (데이터 영구 보존) |
| ngrok | 4040 | Notion Webhook을 로컬에서 수신하기 위한 터널 |
| backend | 8080 | 백엔드 컨테이너 (`just compose-up` 시에만 실행) |

---

## 백엔드 개발

```bash
just bootRun                  # postgres + ngrok 자동 기동 후 백엔드 실행
just test-unit                # 단위
just test-slice               # 슬라이스
just test-component           # 컴포넌트
just test-all                 # 전체
just test-class [ClassName]   # 단일 클래스
```

### 시스템 테스트

```bash
just systest   # Testcontainers가 PostgreSQL · WireMock · backend image 자동 빌드·기동
```

### API 타입 생성

백엔드 OpenAPI 스펙으로부터 TypeScript 타입을 생성한다. 백엔드가 실행 중인 상태에서 실행한다.

```bash
just gen-types   # packages/api-types/src/schema.ts 갱신
```

---

## 프론트엔드 / 인프라 개발

백엔드를 직접 수정하지 않고 프론트엔드나 인프라 작업만 할 때는 백엔드를 컨테이너로 띄운다.

```bash
just compose-up    # postgres + ngrok + 백엔드 컨테이너
just compose-down
```

처음 실행 시 백엔드 이미지를 빌드하므로 시간이 소요된다.

백엔드가 뜨면 Swagger UI(`http://localhost:8080/api/swagger-ui.html`)에서 API 명세를 확인할 수 있다.