package com.sherazsadiq.dermascan.chat



import java.util.Date

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: String = "",

)
