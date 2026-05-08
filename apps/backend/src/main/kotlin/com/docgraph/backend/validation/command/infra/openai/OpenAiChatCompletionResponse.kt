package com.docgraph.backend.validation.command.infra.openai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChatCompletionResponse(
    val choices: List<Choice>,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Choice(val message: OpenAiChatMessage)
}
