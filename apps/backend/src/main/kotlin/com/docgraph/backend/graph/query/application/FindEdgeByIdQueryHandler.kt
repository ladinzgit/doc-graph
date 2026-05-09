package com.docgraph.backend.graph.query.application

import org.springframework.stereotype.Service

@Service
class FindEdgeByIdQueryHandler : FindEdgeByIdQuery {
    override fun find(edgeId: Long): EdgeDetail? = TODO("graph 도메인 query 미구현")
}