package com.docgraph.backend.graph.command.domain

import java.time.OffsetDateTime
import java.util.UUID

data class ValidationPairCreatedEvent(
    val validationPairId: UUID,
    val edgeId: Long,
    val occurredAt: OffsetDateTime,
)