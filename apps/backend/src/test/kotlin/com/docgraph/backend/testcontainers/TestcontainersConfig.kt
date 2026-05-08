package com.docgraph.backend.testcontainers

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> = sharedPostgres

    companion object {
        // static lazy — 모든 Spring 컨텍스트 캐시 분기에서 같은 container 공유.
        // JVM 종료 시 testcontainers ryuk가 정리.
        private val sharedPostgres: PostgreSQLContainer<*> by lazy {
            val version = System.getenv("POSTGRES_VERSION")
                ?: error("POSTGRES_VERSION 환경변수 미설정 — Gradle test task가 .env 로드 못함")
            PostgreSQLContainer("postgres:$version-alpine").apply { start() }
        }
    }
}
