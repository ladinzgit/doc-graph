package com.docgraph.backend.event

import org.springframework.context.ApplicationEventPublisher
import java.time.Duration
import java.time.OffsetDateTime

abstract class AbstractStalePendingScheduler<T : OutboxEntry>(
    private val repository: OutboxRepository<T>,
    private val publisher: ApplicationEventPublisher,
) {
    fun processStaleRows(staleThreshold: Duration) {
        val cutoff = OffsetDateTime.now().minus(staleThreshold)
        repository.findStale(cutoff).forEach { entry ->
            publisher.publishEvent(rehydrate(entry))
        }
    }

    protected abstract fun rehydrate(entry: T): Any
}
