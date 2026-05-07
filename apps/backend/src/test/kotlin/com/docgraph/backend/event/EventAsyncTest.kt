package com.docgraph.backend.event

import com.docgraph.backend.testcontainers.POSTGRES_IMAGE
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

data class SentinelEvent(val id: String)

@Component
class SentinelListener {
    val latch = CountDownLatch(1)
    val firedThread = AtomicReference<String?>(null)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onSentinel(event: SentinelEvent) {
        firedThread.set(Thread.currentThread().name)
        latch.countDown()
    }
}

@Service
class SentinelPublisher(private val publisher: ApplicationEventPublisher) {
    @Transactional
    fun publish(id: String) {
        publisher.publishEvent(SentinelEvent(id))
    }
}

@TestConfiguration
class SentinelConfig {
    @org.springframework.context.annotation.Bean
    fun sentinelListener() = SentinelListener()

    @org.springframework.context.annotation.Bean
    fun sentinelPublisher(publisher: ApplicationEventPublisher) = SentinelPublisher(publisher)
}

@SpringBootTest(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@Testcontainers
@Import(SentinelConfig::class)
class EventAsyncTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer<Nothing>(POSTGRES_IMAGE)
    }

    @Autowired
    lateinit var sentinelPublisher: SentinelPublisher

    @Autowired
    lateinit var sentinelListener: SentinelListener

    @Test
    fun `listener fires asynchronously after publisher transaction commit`() {
        val testThread = Thread.currentThread().name

        sentinelPublisher.publish("test-1")

        val fired = sentinelListener.latch.await(5, TimeUnit.SECONDS)
        assertTrue(fired, "listener did not fire within 5s")

        val firedOn = sentinelListener.firedThread.get()
        assertNotEquals(testThread, firedOn, "listener ran synchronously on test thread; @Async + @EnableAsync 미적용 의심")
    }
}
