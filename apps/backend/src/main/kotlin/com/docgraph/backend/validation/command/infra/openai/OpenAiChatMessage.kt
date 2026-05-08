package com.docgraph.backend.validation.command.infra.openai

data class OpenAiChatMessage(
    val role: String,
    val content: String,
) {
    companion object {
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
    }
}
