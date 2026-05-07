package com.docgraph.backend.graph.query.application

fun interface FindEdgeByIdQuery {
    fun handle(edgeId: Long): EdgeDetail?
}