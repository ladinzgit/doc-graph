package com.docgraph.backend.event

import java.time.Duration
import java.time.OffsetDateTime

enum class OutboxStatus { PENDING, SUCCESS, FAILED }

interface OutboxEntry {
    val id: Long
    var status: OutboxStatus
    var attempts: Int
    var lastAttemptAt: OffsetDateTime?
    var failureReason: String?

    fun isStale(now: OffsetDateTime, threshold: Duration): Boolean =
        status == OutboxStatus.PENDING &&
            (lastAttemptAt == null || lastAttemptAt!!.plus(threshold).isBefore(now))
}
