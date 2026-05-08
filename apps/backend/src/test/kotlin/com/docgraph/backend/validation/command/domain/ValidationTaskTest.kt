package com.docgraph.backend.validation.command.domain

import com.docgraph.backend.event.OutboxStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Tag("unit")
class ValidationTaskTest {

    @Test
    fun `recordAttempt — attempts 증가 + lastAttemptAt 갱신`() {
        val task = ValidationTask(validationPairId = UUID.randomUUID(), edgeId = 100L)
        assertEquals(0, task.attempts)
        assertNull(task.lastAttemptAt)

        task.recordAttempt()

        assertEquals(1, task.attempts)
        assertNotNull(task.lastAttemptAt)

        task.recordAttempt()
        assertEquals(2, task.attempts)
    }

    @Test
    fun `markFailed — status FAILED + failureReason 기록`() {
        val task = ValidationTask(validationPairId = UUID.randomUUID(), edgeId = 100L)

        task.markFailed("test reason")

        assertEquals(OutboxStatus.FAILED, task.status)
        assertEquals("test reason", task.failureReason)
    }

    @Test
    fun `markFailed — null reason 허용`() {
        val task = ValidationTask(validationPairId = UUID.randomUUID(), edgeId = 100L)

        task.markFailed(null)

        assertEquals(OutboxStatus.FAILED, task.status)
        assertNull(task.failureReason)
    }
}
