package com.docgraph.backend

import com.docgraph.backend.fixtures.SharedPostgresContainer
import com.docgraph.backend.validation.command.application.ProcessValidationTaskCommandHandler
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Tag("component")
@SpringBootTest
@Import(SharedPostgresContainer::class)
class BackendApplicationTests {

    // publisher 도메인(graph/document) Query handler 본체가 아직 stub interface만 — 컨텍스트 부팅용 mock.
    @MockkBean lateinit var processHandler: ProcessValidationTaskCommandHandler

    @Test
    fun contextLoads() {
    }
}