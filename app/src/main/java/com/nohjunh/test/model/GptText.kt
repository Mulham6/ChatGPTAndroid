package com.nohjunh.test.model

data class GptText(
    val index: Int,
    val message: Message,
    val logprobs: Any?, // null in most cases
    val finish_reason: String
)

data class Message(
    val role: String,
    val content: String
)