package com.docgraph.backend.fixtures

import org.springframework.test.context.TestPropertySource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@TestPropertySource(
    properties = [
        "ai.openai.api-key=test",
        "ai.openai.model=test-model",
    ]
)
annotation class OpenAiTestFixture
