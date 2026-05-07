package com.docgraph.backend.project.query.application

import com.docgraph.backend.document.query.application.DocumentType
import io.swagger.v3.oas.annotations.media.Schema

data class TypeMappingResponse(
    @Schema(description = "Notion 페이지 ID", example = "abc1234567890def")
    val notionPageId: String,
    @Schema(description = "Notion 페이지 제목", example = "회의록")
    val notionPageTitle: String,
    @Schema(description = "문서 타입")
    val documentType: DocumentType,
)
