package com.sbrati.rastibot.command

import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpContext
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.NoReplyView
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.keyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.service.LocaleSettingsService
import com.sbrati.spring.boot.starter.kotlin.telegram.view.TelegramView
import org.springframework.context.annotation.Configuration

@Configuration
open class SetLanguage(
    private val view: TelegramView,
    private val localeSettingsService: LocaleSettingsService,
): TelegramCommand<NoOpContext>("language", NoOpContext::class.java) {

    init {
        stage("request_language") {
            start { _, _ ->
                message {
                    key = "setlanguage.info.specify.language.from.the.list"
                    replyView = keyboard { row(view.supportedLanguagesButtons()) }
                }
            }
            text { _, text, context ->
                if (!localeSettingsService.isLanguageSupported(text)) {
                    message {
                        key = "setlanguage.error.unsupported.language.specified"
                        args = listOf(context.firstName)
                        replyView = NoReplyView
                    }
                } else {
                    localeSettingsService.updateUserLanguagePreferences(context.chatId!!, text)
                    finish {
                        message(key = "setlanguage.info.language.settings.changed", args = listOf(text))
                    }
                }
            }
        }
    }
}
