package com.docgraph.backend.validation.command.interfaces.event

import com.docgraph.backend.graph.command.domain.ValidationPairCreatedEvent
import com.docgraph.backend.testcontainers.POSTGRES_IMAGE
import com.docgraph.backend.validation.command.application.ProcessValidationTaskCommandHandler
import com.docgraph.backend.validation.command.domain.ValidationTaskQueuedEvent
import com.docgraph.backend.validation.command.domain.ValidationTaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import com.ninjasquad.springmockk.MockkBean
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationPairRollbackProbe {
    val latch = CountDownLatch(1)

    @EventListener
    fun onReady(event: ValidationTaskQueuedEvent) {
        latch.countDown()
        throw RuntimeException("forcing rollback for REQUIRES_NEW test")
    }
}

open class ValidationPairCreatedRollbackPublisher(
    private val publisher: ApplicationEventPublisher,
) {
    @Transactional
    open fun publish(event: ValidationPairCreatedEvent) {
        publisher.publishEvent(event)
    }
}

@TestConfiguration
class ValidationPairCreatedEventListenerRollbackTestConfig {
    @Bean
    fun validationPairRollbackProbe() = ValidationPairRollbackProbe()

    @Bean
    fun validationPairCreatedRollbackPublisher(publisher: ApplicationEventPublisher) =
        ValidationPairCreatedRollbackPublisher(publisher)
}

@Tag("component")
@SpringBootTest
@Testcontainers
@Import(ValidationPairCreatedEventListenerRollbackTestConfig::class)
class ValidationPairCreatedEventListenerRollbackTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer<Nothing>(POSTGRES_IMAGE)
    }

    @Autowired lateinit var testPublisher: ValidationPairCreatedRollbackPublisher

    @Autowired lateinit var probe: ValidationPairRollbackProbe

    @Autowired lateinit var repository: ValidationTaskRepository

    @MockkBean lateinit var processHandler: ProcessValidationTaskCommandHandler

    @BeforeEach
    fun resetState() {
        repository.deleteAll()
    }

    @Test
    fun `does not persist ValidationTask when downstream throws inside REQUIRES_NEW`() {
        testPublisher.publish(
            ValidationPairCreatedEvent(UUID.randomUUID(), 42L, OffsetDateTime.now()),
        )

        // sync ready listener가 호출됐는지 latch로 확인 — 호출됐다면 listener가 save 후 publishEvent까지 도달했다는 뜻
        val attempted = probe.latch.await(5, TimeUnit.SECONDS)
        assertTrue(attempted, "downstream rollback path 진입 실패 — listener가 publishEvent에 도달하지 않음")

        // tx interceptor가 listener method 종료 후 rollback을 처리하므로 buffer
        Thread.sleep(500)

        assertEquals(
            0L,
            repository.count(),
            "ValidationTask가 영속화됐음 — listener의 @Transactional(REQUIRES_NEW)가 rollback하지 못함",
        )
    }
}