package com.docgraph.backend.event

import com.docgraph.backend.testcontainers.POSTGRES_IMAGE
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

data class OutboxSentinelEvent(val payload: String)

class OutboxAttemptProbe {
    val latch = CountDownLatch(1)
}

open class OutboxSentinelListener(
    private val repository: TestOutboxRepository,
    private val probe: OutboxAttemptProbe,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onSentinel(event: OutboxSentinelEvent) {
        try {
            repository.save(TestOutboxEntry(payload = event.payload))
            throw RuntimeException("forcing rollback")
        } finally {
            probe.latch.countDown()
        }
    }
}

open class OutboxSentinelPublisher(private val publisher: ApplicationEventPublisher) {
    @Transactional
    open fun publish(payload: String) {
        publisher.publishEvent(OutboxSentinelEvent(payload))
    }
}

@TestConfiguration
class OutboxSentinelConfig {
    @Bean
    fun outboxAttemptProbe() = OutboxAttemptProbe()

    @Bean
    fun outboxSentinelListener(
        repository: TestOutboxRepository,
        probe: OutboxAttemptProbe,
    ) = OutboxSentinelListener(repository, probe)

    @Bean
    fun outboxSentinelPublisher(publisher: ApplicationEventPublisher) = OutboxSentinelPublisher(publisher)
}

@SpringBootTest(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@Testcontainers
@Import(OutboxSentinelConfig::class)
class EventOutboxTransactionTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer<Nothing>(POSTGRES_IMAGE)
    }

    @Autowired
    lateinit var outboxSentinelPublisher: OutboxSentinelPublisher

    @Autowired
    lateinit var probe: OutboxAttemptProbe

    @Autowired
    lateinit var repository: TestOutboxRepository

    @Test
    fun `listener rolls back persist when method throws`() {
        outboxSentinelPublisher.publish("outbox-1")

        val attempted = probe.latch.await(5, TimeUnit.SECONDS)
        assertTrue(attempted, "listener did not attempt within 5s")

        // tx interceptor가 method 종료 후 rollback을 처리하므로 약간의 buffer
        Thread.sleep(500)

        assertEquals(
            0L,
            repository.count(),
            "entity persisted; rollback 미발생 → listener에 @Transactional(REQUIRES_NEW) 미적용 의심",
        )
    }
}
