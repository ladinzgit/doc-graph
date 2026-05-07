package com.docgraph.backend.project.query.application

import io.swagger.v3.oas.annotations.media.Schema

data class ProjectDetail(
    @Schema(description = "프로젝트 ID", example = "1")
    val id: Long,
    @Schema(description = "프로젝트 이름", example = "Q2 Planning")
    val name: String,
    @Schema(description = "Notion 루트 페이지 ID", example = "abc1234567890def")
    val notionRootPageId: String,
    val members: List<ProjectMemberSummary>,
)
