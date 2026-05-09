import os

import httpx
import psycopg
import pytest

BACKEND_BASE_URL = "http://localhost:8080/api"
WIREMOCK_BASE_URL = "http://localhost:8081"
PG_DSN = (
    "host=localhost port=5434"
    f" user={os.environ['DB_USERNAME']}"
    f" password={os.environ['DB_PASSWORD']}"
    " dbname=docgraph"
)


@pytest.fixture(scope="session")
def client():
    with httpx.Client(base_url=BACKEND_BASE_URL) as c:
        yield c


@pytest.fixture(scope="session")
def wiremock():
    return WireMockHelper(WIREMOCK_BASE_URL)


@pytest.fixture(autouse=True)
def _isolate(wiremock):
    wiremock.reset()
    _truncate_all_tables()
    yield


def _truncate_all_tables() -> None:
    with psycopg.connect(PG_DSN) as conn, conn.cursor() as cur:
        cur.execute(
            """
            SELECT string_agg(format('%I.%I', schemaname, tablename), ',')
            FROM pg_tables
            WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
              AND tablename != 'flyway_schema_history'
            """
        )
        row = cur.fetchone()
        tables = row[0] if row else None
        if tables:
            cur.execute(f"TRUNCATE {tables} CASCADE")


@pytest.fixture
def auth_headers():
    return {"X-Test-User-Id": "test-user-1"}


class WireMockHelper:
    def __init__(self, base_url: str):
        self.base_url = base_url

    def stub(self, request: dict, response: dict) -> httpx.Response:
        return httpx.post(
            f"{self.base_url}/__admin/mappings",
            json={"request": request, "response": response},
        )

    def reset(self) -> None:
        httpx.post(f"{self.base_url}/__admin/reset")

    def calls(self) -> list:
        return httpx.get(f"{self.base_url}/__admin/requests").json().get("requests", [])