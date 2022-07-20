package com.sbrati.rastibot.model.callback

import com.github.kotlintelegrambot.entities.Update
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.CallbackDataObject

class ChatIdCallbackData : CallbackDataObject() {

    lateinit var keyString: String
    var chatId: Long = 0

    override fun asString(): String {
        return "${getKey()}|$chatId"
    }

    override fun construct(value: String) {
        val keyAndChatIdSpec = value.split("|")
        if (keyAndChatIdSpec.size != 2) {
            throw IllegalStateException("Failed to instantiate ChatIdCallbackData from string '$value'.")
        }
        this.keyString = keyAndChatIdSpec[0]
        this.chatId = keyAndChatIdSpec[1].toLong()
    }

    override fun getKey(): String {
        return keyString
    }
}

fun chatIdCallback(key: String, chatId: Long): ChatIdCallbackData {
    val chatIdCallbackData = ChatIdCallbackData()
    chatIdCallbackData.chatId = chatId
    chatIdCallbackData.keyString = key
    return chatIdCallbackData
}

fun Update.getChatIdFromCallbackData(): Long? {
    val callbackDataString = this.callbackQuery?.data
    return callbackDataString?.let {
        val chatIdCallbackData = ChatIdCallbackData()
        chatIdCallbackData.construct(callbackDataString)
        return chatIdCallbackData.chatId
    }
}