package com.cleanai.libs.llm

import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.retry.RetryConfig
import ai.koog.prompt.executor.clients.retry.RetryingLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import com.cleanai.modules.logbook.infrastructure.config.AiConfig
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class LLMProvider(
    private val aiConfig: AiConfig
) {

    fun getGeminiExecutor(): SingleLLMPromptExecutor {


        val resilientClient = RetryingLLMClient(
            GoogleLLMClient(
                apiKey = aiConfig.gemini().apiKey(),
            ),
            RetryConfig.PRODUCTION
        )
        return SingleLLMPromptExecutor(resilientClient)
    }


}
