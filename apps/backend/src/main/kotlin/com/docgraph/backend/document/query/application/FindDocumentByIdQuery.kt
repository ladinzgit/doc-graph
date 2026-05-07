package com.docgraph.backend.document.query.application

fun interface FindDocumentByIdQuery {
    fun handle(documentId: Long): DocumentDetail?
}