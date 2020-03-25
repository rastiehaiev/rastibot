package com.sbrati.rastibot.instruments

import com.sbrati.rastibot.model.Birthday
import com.sbrati.spring.boot.starter.kotlin.telegram.resolver.locale.TelegramLocaleResolver
import com.sbrati.spring.boot.starter.kotlin.telegram.util.toFormattedDate
import org.springframework.stereotype.Component

@Component
class BirthDayHelper(private val localeResolver: TelegramLocaleResolver) {

    fun dateToString(chatId: Long, birthday: Birthday): String {
        val locale = localeResolver.resolve(chatId)
        return toFormattedDate(locale, birthday.month, birthday.day, birthday.year)
    }
}