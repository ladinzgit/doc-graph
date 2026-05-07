package com.docgraph.backend.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime

@Entity
@Table(name = "test_outbox_entry")
class TestOutboxEntry(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long = 0L,

    @Enumerated(EnumType.STRING)
    override var status: OutboxStatus = OutboxStatus.PENDING,

    override var attempts: Int = 0,

    override var lastAttemptAt: OffsetDateTime? = null,

    @Column(length = 1000)
    override var failureReason: String? = null,

    @Column(nullable = false)
    var payload: String = "",
) : OutboxEntry

interface TestOutboxRepository : JpaRepository<TestOutboxEntry, Long>, OutboxRepository<TestOutboxEntry> {
    @Query("SELECT e FROM TestOutboxEntry e WHERE e.status = 'PENDING' AND (e.lastAttemptAt IS NULL OR e.lastAttemptAt < :before)")
    override fun findStale(before: OffsetDateTime): List<TestOutboxEntry>
}
