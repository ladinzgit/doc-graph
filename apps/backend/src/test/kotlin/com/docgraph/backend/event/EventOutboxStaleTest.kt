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
import org.springframework.context.event.EventListener
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals

data class StaleRehydratedEvent(val entryId: Long)

class StaleProbe {
    val received = CopyOnWriteArrayList<StaleRehydratedEvent>()
}

class TestStaleScheduler(
    repository: OutboxRepository<TestOutboxEntry>,
    publisher: ApplicationEventPublisher,
) : AbstractStalePendingScheduler<TestOutboxEntry>(repository, publisher) {
    override fun rehydrate(entry: TestOutboxEntry): Any = StaleRehydratedEvent(entry.id)
}

class TestStaleProbeListener(private val probe: StaleProbe) {
    @EventListener
    fun onRehydrated(event: StaleRehydratedEvent) {
        probe.received.add(event)
    }
}

@TestConfiguration
class StaleSchedulerTestConfig {
    @Bean
    fun staleProbe() = StaleProbe()

    @Bean
    fun testStaleScheduler(
        repository: TestOutboxRepository,
        publisher: ApplicationEventPublisher,
    ) = TestStaleScheduler(repository, publisher)

    @Bean
    fun testStaleProbeListener(probe: StaleProbe) = TestStaleProbeListener(probe)
}

@SpringBootTest(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@Testcontainers
@Import(StaleSchedulerTestConfig::class)
class EventOutboxStaleTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer<Nothing>(POSTGRES_IMAGE)
    }

    @Autowired
    lateinit var repository: TestOutboxRepository

    @Autowired
    lateinit var scheduler: TestStaleScheduler

    @Autowired
    lateinit var probe: StaleProbe

    @Test
    fun `scheduler republishes event for stale pending row`() {
        val seeded = repository.save(
            TestOutboxEntry(
                payload = "stale-1",
                lastAttemptAt = OffsetDateTime.now().minusMinutes(10),
            ),
        )

        scheduler.processStaleRows(Duration.ofMinutes(5))

        assertEquals(1, probe.received.size, "scheduler should have republished one event")
        assertEquals(seeded.id, probe.received[0].entryId)
    }
}
