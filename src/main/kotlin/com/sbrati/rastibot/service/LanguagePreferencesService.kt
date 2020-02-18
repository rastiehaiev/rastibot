package com.sbrati.rastibot.service

import com.sbrati.rastibot.setLanguage
import com.sbrati.spring.starter.telegram.dsl.Transaction
import com.sbrati.spring.starter.telegram.exception.TelegramUpdateProcessingException
import com.sbrati.spring.starter.telegram.locale.LocaleRepository
import com.sbrati.spring.starter.telegram.model.key
import com.sbrati.spring.starter.telegram.utils.getLocale
import com.sbrati.spring.starter.telegram.utils.resolveLocaleFromMessage
import me.ivmg.telegram.entities.Update
import org.springframework.stereotype.Service

@Service
class LanguagePreferencesService(private val localeRepository: LocaleRepository) {

    fun updateLanguagePreferences(update: Update, transaction: Transaction): String {
        val chatId = update.message?.chat?.id!!
        val (name, locale) = update.resolveLocaleFromMessage()
                ?: throw TelegramUpdateProcessingException(key("error.message.should.contain.supported.language"), update.getLocale())
        localeRepository.save(chatId, locale)
        transaction.setLanguage(name)
        return name
    }
}