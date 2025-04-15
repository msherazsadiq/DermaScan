package com.sherazsadiq.dermascan.chat



import java.util.Date

data class ChatMessage(
    val message: String,
    val user: Boolean,
    val timestamp: String = "",

) {
    constructor() : this("", false, "")
}

data class ChatData(
    val message: String,
    val user: Boolean,
    val timestamp: String = "",

    ) {
    constructor() : this("", false, "")
}
