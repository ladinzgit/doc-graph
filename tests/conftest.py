import os
import time
from pathlib import Path

import httpx
import pytest
from testcontainers.core.container import DockerContainer
from testcontainers.core.image import DockerImage
from testcontainers.core.network import Network
from testcontainers.postgres import PostgresContainer

PROJECT_ROOT = Path(__file__).parent.parent
POSTGRES_VERSION = os.environ["POSTGRES_VERSION"]


@pytest.fixture(scope="session")
def network():
    network = Network()
    network.create()
    yield network
    network.remove()


@pytest.fixture(scope="session")
def postgres(network):
    container = (
        PostgresContainer(f"postgres:{POSTGRES_VERSION}-alpine")
        .with_network(network)
        .with_network_aliases("postgres")
    )
    with container as pg:
        yield pg


@pytest.fixture(scope="session")
def wiremock(network):
    container = (
        DockerContainer("wiremock/wiremock:latest")
        .with_network(network)
        .with_network_aliases("wiremock")
        .with_exposed_ports(8080)
    )
    container.start()
    host = container.get_container_host_ip()
    port = container.get_exposed_port(8080)
    base_url = f"http://{host}:{port}"
    _wait_for_url(f"{base_url}/__admin/mappings")
    yield WireMockHelper(base_url)
    container.stop()


@pytest.fixture(scope="session")
def backend_image():
    image = DockerImage(
        path=str(PROJECT_ROOT / "apps" / "backend"),
        tag="doc-graph-backend:test",
    )
    image.build()
    return image.tag


@pytest.fixture(scope="session")
def backend(backend_image, postgres, wiremock, network):
    container = (
        DockerContainer(backend_image)
        .with_network(network)
        .with_env("SPRING_DATASOURCE_URL", f"jdbc:postgresql://postgres:5432/{postgres.dbname}")
        .with_env("SPRING_DATASOURCE_USERNAME", postgres.username)
        .with_env("SPRING_DATASOURCE_PASSWORD", postgres.password)
        .with_exposed_ports(8080)
    )
    container.start()
    host = container.get_container_host_ip()
    port = container.get_exposed_port(8080)
    base_url = f"http://{host}:{port}"
    _wait_for_url(f"{base_url}/actuator/health")
    yield base_url
    container.stop()


@pytest.fixture(scope="session")
def client(backend):
    with httpx.Client(base_url=backend) as c:
        yield c


@pytest.fixture
def auth_headers():
    """test profile에서 X-Test-User-Id로 인증 우회.

    실제 OAuth 흐름은 슬라이스 테스트에서 검증. 시스템 테스트는
    인증된 컨텍스트에서의 비즈니스 흐름에 집중.
    """
    return {"X-Test-User-Id": "test-user-1"}


@pytest.fixture(autouse=True)
def _wiremock_reset(wiremock):
    wiremock.reset()
    yield


def _wait_for_url(url: str, timeout: int = 180):
    deadline = time.time() + timeout
    last_error: Exception | None = None
    while time.time() < deadline:
        try:
            r = httpx.get(url, timeout=2)
            if r.status_code == 200:
                return
        except Exception as e:
            last_error = e
        time.sleep(2)
    raise TimeoutError(f"{url} did not respond within {timeout}s. Last error: {last_error}")


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
