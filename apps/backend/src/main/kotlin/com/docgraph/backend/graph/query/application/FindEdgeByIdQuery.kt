package com.docgraph.backend.graph.query.application

fun interface FindEdgeByIdQuery {
    fun find(edgeId: Long): EdgeDetail?
}