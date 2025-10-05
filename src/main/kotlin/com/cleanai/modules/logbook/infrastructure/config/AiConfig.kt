package com.cleanai.modules.logbook.infrastructure.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "app.ai")
interface AiConfig {
    fun gemini(): GeminiConfig
    
    interface GeminiConfig {
        fun apiKey(): String
    }
}
