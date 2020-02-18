package com.sbrati.rastibot.component

import com.sbrati.rastibot.client.UserServiceClient
import com.sbrati.rastibot.model.User
import com.sbrati.spring.starter.telegram.locale.LocaleRepository
import com.sbrati.spring.starter.telegram.utils.LoggerDelegate
import org.springframework.stereotype.Component
import java.util.*

@Component
class PersistentLocaleRepository(private val userServiceClient: UserServiceClient) : LocaleRepository {

    private val logger by LoggerDelegate()

    override fun find(chatId: Long): Locale? {
        return try {
            val user = userServiceClient.findByChatId(chatId)
            user?.language?.run { Locale(this) }
        } catch (e: Exception) {
            logger.error("User service is not available. Reason: ${e.message}.")
            null
        }
    }

    override fun save(chatId: Long, locale: Locale) {
        val user = userServiceClient.findByChatId(chatId) ?: User(chatId = chatId)
        user.language = locale.language
        userServiceClient.create(user)
    }
}