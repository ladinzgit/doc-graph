# Windows default shell을 PowerShell → sh로 강제 (Unix는 영향 없음 — 기본이 sh)
set windows-shell := ["sh", "-cu"]

# .env(평문 + dotenvx 암호화) + .env.local(개인 시크릿)을 child process에 주입.
# --overload: .env.local이 .env의 빈 placeholder를 override (last-wins).
# .env.keys로 암호화 항목 복호화.
dotenv-run := 'dotenvx run --strict --overload -f .env -f .env.local --'

# 기본 — 사용 가능한 recipe 목록
default:
    @just --list

# 백엔드 로컬 개발 (postgres + ngrok 헬스체크 통과 후 백엔드 실행)
bootRun:
    {{dotenv-run}} sh -c 'docker compose up -d --wait && cd apps/backend && ./gradlew bootRun'

# 백엔드 테스트 — 외부 시크릿은 dotenvx 미적용으로 환경에서 차단
test-unit:
    cd apps/backend && exec ./gradlew unitTest

test-slice:
    cd apps/backend && exec ./gradlew sliceTest

test-component:
    cd apps/backend && exec ./gradlew componentTest

test-all:
    cd apps/backend && exec ./gradlew test

test-class class:
    cd apps/backend && exec ./gradlew test --tests {{class}}

# 풀 스택 — postgres + ngrok + backend 컨테이너
compose-up:
    {{dotenv-run}} docker compose --profile full up

compose-down:
    {{dotenv-run}} docker compose down

# Pytest 시스템 테스트 — backend 컨테이너 + wiremock에 외부 client로 접근
systest:
    #!/usr/bin/env sh
    {{dotenv-run}} sh -c "trap 'docker compose -p doc-graph-test -f docker-compose.yml -f docker-compose.test.yml --profile full down -v' EXIT && docker compose -p doc-graph-test -f docker-compose.yml -f docker-compose.test.yml --profile full up -d --build --wait && ( cd tests && uv run pytest )"

# OpenAPI → TypeScript 타입 생성 (백엔드 실행 중이어야 함)
gen-types:
    npm run generate:types

# 팀 공유 시크릿 암호화 후 .env에 저장
encrypt-secret key value:
    dotenvx set {{key}} {{value}}
