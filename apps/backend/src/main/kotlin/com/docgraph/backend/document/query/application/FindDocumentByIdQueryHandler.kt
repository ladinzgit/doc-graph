package com.docgraph.backend.document.query.application

import org.springframework.stereotype.Service

@Service
class FindDocumentByIdQueryHandler : FindDocumentByIdQuery {
    override fun find(documentId: Long): DocumentDetail? = TODO("document 도메인 query 미구현")
}