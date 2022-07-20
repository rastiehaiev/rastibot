package com.sbrati.rastibot.service

import com.sbrati.telegram.domain.Event
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class EventCreationService(private val clock: Clock) {

    fun <T> create(chatId: Long, payload: T): Event<T> {
        return create(chatId, payload, false)
    }

    fun <T> createGlobal(chatId: Long, payload: T): Event<T> {
        return create(chatId, payload, true)
    }

    private fun <T> create(chatId: Long, payload: T, global: Boolean): Event<T> {
        val event = Event<T>()
        event.payload = payload
        event.chatId = chatId
        event.timestamp = clock.millis()
        event.isGlobal = global
        return event
    }
}