package com.docgraph.backend.validation.command.interfaces.event

import com.docgraph.backend.validation.command.application.ProcessValidationTaskCommand
import com.docgraph.backend.validation.command.application.ProcessValidationTaskCommandHandler
import com.docgraph.backend.validation.command.domain.ValidationTaskQueuedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ValidationTaskQueuedEventListener(
    private val handler: ProcessValidationTaskCommandHandler,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ValidationTaskQueuedEvent) {
        handler.handle(ProcessValidationTaskCommand(event.taskId))
    }
}
