package com.docgraph.backend.validation.command.domain

import com.docgraph.backend.event.OutboxRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.UUID

interface ValidationTaskRepository :
    JpaRepository<ValidationTask, Long>,
    OutboxRepository<ValidationTask> {

    @Query(
        """
        SELECT t FROM ValidationTask t
        WHERE t.status = 'PENDING'
          AND (t.lastAttemptAt IS NULL OR t.lastAttemptAt < :before)
        """,
    )
    override fun findStale(before: OffsetDateTime): List<ValidationTask>

    fun findByValidationPairId(validationPairId: UUID): ValidationTask?
}