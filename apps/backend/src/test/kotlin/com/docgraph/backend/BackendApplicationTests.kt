package com.docgraph.backend

import com.docgraph.backend.testcontainers.POSTGRES_IMAGE
import com.docgraph.backend.validation.command.application.ProcessValidationTaskCommandHandler
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Tag("component")
@SpringBootTest
@Testcontainers
class BackendApplicationTests {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer<Nothing>(POSTGRES_IMAGE)
    }

    // publisher 도메인(graph/document) Query handler 본체가 아직 stub interface만 — 컨텍스트 부팅용 mock.
    @MockkBean lateinit var processHandler: ProcessValidationTaskCommandHandler

    @Test
    fun contextLoads() {
    }
}
