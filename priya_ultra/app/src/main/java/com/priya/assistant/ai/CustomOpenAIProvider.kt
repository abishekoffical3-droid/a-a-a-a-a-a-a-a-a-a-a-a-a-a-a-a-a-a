package com.priya.assistant.ai

/**
 * Any OpenAI-compatible chat-completions endpoint (LM Studio, Ollama's OpenAI shim,
 * OpenRouter, self-hosted gateways, etc). The base URL is mandatory and always
 * supplied by the caller — there is no assumption about which service it is.
 */
class CustomOpenAIProvider : OpenAIProvider(
    id = "custom",
    displayName = "Custom Provider",
    defaultBaseUrl = "" // must be supplied by the user; enforced in generateReply via baseUrl param
)
