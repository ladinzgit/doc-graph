# tests

시스템 테스트 — Testcontainers가 PostgreSQL · WireMock · backend image를 자동 빌드·기동한 상태에서 pytest로 API 직접 호출.

## 실행

```bash
uv run pytest
```

Docker 데몬이 떠 있어야 한다. 첫 실행 시 backend image 빌드로 시간이 소요된다.
