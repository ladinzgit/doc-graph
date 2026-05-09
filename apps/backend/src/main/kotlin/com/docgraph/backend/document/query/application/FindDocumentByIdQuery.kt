package com.docgraph.backend.document.query.application

fun interface FindDocumentByIdQuery {
    fun find(documentId: Long): DocumentDetail?
}