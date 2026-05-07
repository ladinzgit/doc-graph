package com.docgraph.backend.workspace.query.application

import io.swagger.v3.oas.annotations.media.Schema

data class MemberSummary(
    @Schema(description = "멤버 ID", example = "1")
    val id: Long,
    @Schema(description = "이름", example = "홍길동")
    val name: String,
    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
)
