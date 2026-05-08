package com.docgraph.backend.validation.command.interfaces.event

import com.docgraph.backend.graph.command.domain.ValidationPairCreatedEvent
import com.docgraph.backend.validation.command.application.EnqueueValidationTaskCommand
import com.docgraph.backend.validation.command.application.EnqueueValidationTaskCommandHandler
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ValidationPairCreatedEventListener(
    private val handler: EnqueueValidationTaskCommandHandler,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ValidationPairCreatedEvent) {
        handler.handle(
            EnqueueValidationTaskCommand(
                validationPairId = event.validationPairId,
                edgeId = event.edgeId,
            ),
        )
    }
}