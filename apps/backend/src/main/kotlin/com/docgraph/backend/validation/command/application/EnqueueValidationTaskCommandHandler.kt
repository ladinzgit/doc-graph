package com.docgraph.backend.validation.command.application

import com.docgraph.backend.validation.command.domain.ValidationTask
import com.docgraph.backend.validation.command.domain.ValidationTaskQueuedEvent
import com.docgraph.backend.validation.command.domain.ValidationTaskRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class EnqueueValidationTaskCommandHandler(
    private val repository: ValidationTaskRepository,
    private val publisher: ApplicationEventPublisher,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handle(command: EnqueueValidationTaskCommand) {
        repository.findByValidationPairId(command.validationPairId)?.let { return }

        val task = repository.save(
            ValidationTask(
                validationPairId = command.validationPairId,
                edgeId = command.edgeId,
            ),
        )
        publisher.publishEvent(ValidationTaskQueuedEvent(task.id))
    }
}