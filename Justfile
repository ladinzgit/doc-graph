# Windows default shell을 PowerShell → sh로 강제 (Unix는 영향 없음 — 기본이 sh)
set windows-shell := ["sh", "-cu"]

# .env(평문 + dotenvx 암호화) + .env.local(개인 시크릿)을 child process에 주입.
# --overload: .env.local이 .env의 빈 placeholder를 override (last-wins).
# .env.keys로 암호화 항목 복호화.
# env 파일 경로는 justfile_directory() 절대경로 → recipe가 cwd를 자유롭게 옮겨도 안전.
dotenv-run := 'dotenvx run --strict --overload -f "' + (justfile_directory() / ".env") + '" -f "' + (justfile_directory() / ".env.local") + '" --'

# 기본 — 사용 가능한 recipe 목록
default:
    @just --list

# 백엔드 로컬 개발 (postgres + ngrok 헬스체크 통과 후 백엔드 실행)
bootRun:
    {{dotenv-run}} docker compose up -d --wait
    cd apps/backend && {{dotenv-run}} sh ./gradlew bootRun

# 백엔드 테스트 — bootRun과 동일하게 dotenvx로 .env + .env.local 주입.
# fixture 결정성은 TestPropertySource가 env 위 precedence로 강제.
test-unit:
    cd apps/backend && {{dotenv-run}} sh ./gradlew unitTest

test-slice:
    cd apps/backend && {{dotenv-run}} sh ./gradlew sliceTest

test-component:
    cd apps/backend && {{dotenv-run}} sh ./gradlew componentTest

test-all:
    cd apps/backend && {{dotenv-run}} sh ./gradlew test

test-class class:
    cd apps/backend && {{dotenv-run}} sh ./gradlew test --tests {{class}}

# 풀 스택 — postgres + ngrok + backend 컨테이너
compose-up:
    {{dotenv-run}} docker compose --profile full up

compose-down:
    {{dotenv-run}} docker compose down

# Pytest 시스템 테스트 — backend 컨테이너 + wiremock에 외부 client로 접근
systest:
    #!/usr/bin/env sh
    set -e
    trap '{{dotenv-run}} docker compose -p doc-graph-test -f docker-compose.yml -f docker-compose.test.yml --profile full down -v' EXIT
    {{dotenv-run}} docker compose -p doc-graph-test -f docker-compose.yml -f docker-compose.test.yml --profile full up -d --build --wait
    cd tests && {{dotenv-run}} uv run pytest

# OpenAPI JSON dump — backend가 forked로 부팅하여 spec dump 후 packages/api-types로 canonical 위치 복사
openapi-dump:
    #!/usr/bin/env sh
    set -e
    trap '{{dotenv-run}} docker compose down' EXIT
    {{dotenv-run}} docker compose up -d --wait
    (cd apps/backend && {{dotenv-run}} sh ./gradlew --rerun-tasks generateOpenApiDocs)
    cp apps/backend/build/openapi.json packages/api-types/openapi.json

# OpenAPI → TypeScript 타입 생성 (packages/api-types/openapi.json 입력)
gen-types: openapi-dump
    npm run generate:types

# Redoc HTML 미리보기 빌드 (apps/docs workspace, 산출물은 apps/docs/dist/index.html)
gen-redoc: openapi-dump
    npm --workspace apps/docs run build

# 팀 공유 시크릿 암호화 후 .env에 저장
encrypt-secret key value:
    dotenvx set {{key}} {{value}}