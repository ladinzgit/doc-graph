package com.docgraph.backend.validation.command.interfaces.scheduler

import com.docgraph.backend.event.AbstractStalePendingScheduler
import com.docgraph.backend.validation.command.domain.ValidationTask
import com.docgraph.backend.validation.command.domain.ValidationTaskQueuedEvent
import com.docgraph.backend.validation.command.domain.ValidationTaskRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ValidationTaskStaleScheduler(
    repository: ValidationTaskRepository,
    publisher: ApplicationEventPublisher,
) : AbstractStalePendingScheduler<ValidationTask>(repository, publisher) {

    override fun rehydrate(entry: ValidationTask): Any =
        ValidationTaskQueuedEvent(entry.id)

    @Scheduled(fixedRate = INTERVAL_MS)
    fun trigger() {
        processStaleRows(THRESHOLD)
    }

    companion object {
        private val THRESHOLD: Duration = Duration.ofMinutes(5)
        private const val INTERVAL_MS: Long = 5L * 60 * 1000
    }
}
