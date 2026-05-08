package com.docgraph.backend.validation.command.application

import java.util.UUID

data class EnqueueValidationTaskCommand(
    val validationPairId: UUID,
    val edgeId: Long,
)
