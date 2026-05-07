package com.docgraph.backend.graph.query.application

data class ProjectGraphResponse(
    val nodes: List<GraphNodeResponse>,
    val edges: List<EdgeResponse>,
    val proposals: List<EdgeProposalResponse>,
)