package com.docgraph.backend.project.query.application

import com.docgraph.backend.document.query.application.DocumentType
import io.swagger.v3.oas.annotations.media.Schema

data class TypeAssigneeResponse(
    @Schema(description = "문서 타입")
    val documentType: DocumentType,
    @Schema(description = "담당자 워크스페이스 멤버 ID (null이면 담당자 없음)", example = "1")
    val assigneeMemberId: Long?,
)
