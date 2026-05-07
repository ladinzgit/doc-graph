package com.docgraph.backend.event

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OutboxEntryStaleTest {

    private fun entry(status: OutboxStatus, lastAttemptAt: OffsetDateTime?): OutboxEntry =
        object : OutboxEntry {
            override val id = 1L
            override var status = status
            override var attempts = 0
            override var lastAttemptAt = lastAttemptAt
            override var failureReason: String? = null
        }

    private val now = OffsetDateTime.parse("2026-01-01T00:00:00Z")
    private val threshold = Duration.ofMinutes(5)

    @Test
    fun `pending with null lastAttemptAt is stale`() {
        assertTrue(entry(OutboxStatus.PENDING, null).isStale(now, threshold))
    }

    @Test
    fun `pending with lastAttemptAt before threshold is stale`() {
        val old = now.minusMinutes(10)
        assertTrue(entry(OutboxStatus.PENDING, old).isStale(now, threshold))
    }

    @Test
    fun `pending with lastAttemptAt within threshold is not stale`() {
        val recent = now.minusMinutes(1)
        assertFalse(entry(OutboxStatus.PENDING, recent).isStale(now, threshold))
    }

    @Test
    fun `success is never stale even with old lastAttemptAt`() {
        val old = now.minusMinutes(10)
        assertFalse(entry(OutboxStatus.SUCCESS, old).isStale(now, threshold))
    }

    @Test
    fun `failed is never stale even with old lastAttemptAt`() {
        val old = now.minusMinutes(10)
        assertFalse(entry(OutboxStatus.FAILED, old).isStale(now, threshold))
    }
}
