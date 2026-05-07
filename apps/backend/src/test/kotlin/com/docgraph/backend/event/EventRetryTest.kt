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
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

data class RetrySentinelEvent(val id: String)

class RetryProbe {
    val attempts = AtomicInteger(0)
    val latch = CountDownLatch(1)
}

open class RetrySentinelListener(private val probe: RetryProbe) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 2, backoff = Backoff(delay = 10))
    open fun onSentinel(event: RetrySentinelEvent) {
        val attempt = probe.attempts.incrementAndGet()
        if (attempt < 2) {
            throw RuntimeException("simulated transient failure")
        }
        probe.latch.countDown()
    }
}

open class RetrySentinelPublisher(private val publisher: ApplicationEventPublisher) {
    @Transactional
    open fun publish(id: String) {
        publisher.publishEvent(RetrySentinelEvent(id))
    }
}

@TestConfiguration
class RetrySentinelConfig {
    @Bean
    fun retryProbe() = RetryProbe()

    @Bean
    fun retrySentinelListener(probe: RetryProbe) = RetrySentinelListener(probe)

    @Bean
    fun retrySentinelPublisher(publisher: ApplicationEventPublisher) = RetrySentinelPublisher(publisher)
}

@SpringBootTest(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@Testcontainers
@Import(RetrySentinelConfig::class)
class EventRetryTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer<Nothing>(POSTGRES_IMAGE)
    }

    @Autowired
    lateinit var retrySentinelPublisher: RetrySentinelPublisher

    @Autowired
    lateinit var probe: RetryProbe

    @Test
    fun `listener retries on transient failure and eventually succeeds`() {
        retrySentinelPublisher.publish("retry-1")

        val fired = probe.latch.await(5, TimeUnit.SECONDS)
        assertTrue(fired, "listener did not succeed within 5s; @EnableRetry 미적용 의심")
        assertEquals(2, probe.attempts.get(), "listener should have been retried once before success")
    }
}
