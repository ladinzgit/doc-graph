package com.docgraph.backend.document.query.application

import com.fasterxml.jackson.annotation.JsonProperty

enum class DocumentType {
    @JsonProperty("meeting_notes") MEETING_NOTES,
    @JsonProperty("planning") PLANNING,
    @JsonProperty("requirements") REQUIREMENTS,
    @JsonProperty("design") DESIGN,
    @JsonProperty("research") RESEARCH,
}
