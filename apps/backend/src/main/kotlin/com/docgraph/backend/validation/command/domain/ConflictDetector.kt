package com.docgraph.backend.validation.command.domain

import com.docgraph.backend.document.query.application.Block

fun interface ConflictDetector {
    fun detect(
        changedBlocks: List<Block>,
        counterpartBlocks: List<Block>,
        criterion: String,
    ): List<DetectedConflict>
}