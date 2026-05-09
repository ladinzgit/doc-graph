# Just가 .env를 자동 로드해 OS 환경변수로 export
set dotenv-load := true

# 시크릿(.env.local)을 OS 환경변수로 추가 export하는 sh fragment.
# 각 recipe가 단일 sh -c 호출 안에서 source 후 도구를 실행한다.
load-secrets := 'set -a; [ -f .env.local ] && . ./.env.local; set +a'

# 기본 — 사용 가능한 recipe 목록
default:
    @just --list

# 첫 온보딩 — .env.local 생성 + JS 의존성 설치
setup:
    sh -c 'touch -a .env.local && npm install'

# 백엔드 로컬 개발 (postgres + ngrok 헬스체크 통과 후 백엔드 실행)
bootRun:
    sh -c '{{load-secrets}}; docker compose up -d --wait && cd apps/backend && exec ./gradlew bootRun'

# 백엔드 테스트 — 외부 시크릿은 build.gradle.kts가 환경에서 차단
test-unit:
    sh -c 'cd apps/backend && exec ./gradlew unitTest'

test-slice:
    sh -c 'cd apps/backend && exec ./gradlew sliceTest'

test-component:
    sh -c 'cd apps/backend && exec ./gradlew componentTest'

test-all:
    sh -c 'cd apps/backend && exec ./gradlew test'

test-class class:
    sh -c 'cd apps/backend && exec ./gradlew test --tests {{class}}'

# 풀 스택 — postgres + ngrok + backend 컨테이너
compose-up:
    sh -c '{{load-secrets}}; exec docker compose --profile full up'

compose-down:
    sh -c '{{load-secrets}}; exec docker compose down'

# Pytest 시스템 테스트 — backend 컨테이너 + wiremock에 외부 client로 접근
systest:
    #!/usr/bin/env sh
    set -a; [ -f .env.local ] && . ./.env.local; set +a
    docker compose -p doc-graph-test -f docker-compose.yml -f docker-compose.test.yml --profile full up -d --build --wait
    ec=$?
    if [ $ec -eq 0 ]; then
        ( cd tests && uv run pytest )
        ec=$?
    fi
    docker compose -p doc-graph-test -f docker-compose.yml -f docker-compose.test.yml down -v
    exit $ec

# OpenAPI → TypeScript 타입 생성 (백엔드 실행 중이어야 함)
gen-types:
    npm run generate:types