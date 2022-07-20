package com.sbrati.rastibot.model

import java.util.Base64

data class Person(
    val chatId: Long?,
    val firstName: String,
    val lastName: String?
) {

    fun getUserId(): String {
        return chatId?.toString()
            ?: arrayOf(firstName, lastName)
                .mapNotNull { it }
                .joinToString(separator = " ")
                .let { Base64.getEncoder().encodeToString(it.toByteArray()) }
    }
}