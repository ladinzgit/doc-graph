package com.docgraph.backend.validation.command.domain

import java.time.OffsetDateTime

data class ValidationTaskPreparedEvent(
    val validationTaskId: Long,
    val occurredAt: OffsetDateTime,
)