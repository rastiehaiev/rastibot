package com.sbrati.rastibot.service

import com.sbrati.rastibot.model.User
import com.sbrati.spring.boot.starter.kotlin.telegram.service.LocaleService
import org.springframework.stereotype.Component
import java.util.*

@Component
class PersistentLocaleService(private val userService: RastiBotUserService) : LocaleService {

    override fun find(chatId: Long): Locale? {
        return userService.findByChatId(chatId)?.locale?.let { Locale(it) }
    }

    override fun save(chatId: Long, locale: Locale) {
        val user = userService.findByChatId(chatId) ?: User(chatId = chatId)
        if (user.locale != locale.language) {
            user.locale = locale.language
            userService.saveOrUpdate(user)
        }
    }
}