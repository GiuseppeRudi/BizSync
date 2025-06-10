package com.bizsync.backend.prompts

object AiPrompts {

    fun cleanAiResponse(response: String): String {
        return response.trim()
            .removePrefix("```json")
            .removeSuffix("```")
            .trim()
    }
}