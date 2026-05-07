package com.docgraph.backend.event

import java.time.OffsetDateTime

interface OutboxRepository<T : OutboxEntry> {
    fun findStale(before: OffsetDateTime): List<T>
}
