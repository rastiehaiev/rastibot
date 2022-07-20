package com.sbrati.rastibot.configuration

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.rastibot.instruments.DateHelper
import com.sbrati.rastibot.model.Notification
import com.sbrati.rastibot.model.callback.ChatIdCallbackData
import com.sbrati.rastibot.model.callback.DeleteBirthdayReminderCallbackData
import com.sbrati.rastibot.model.callback.NotificationActionCallback
import com.sbrati.rastibot.model.callback.notificationAction
import com.sbrati.rastibot.model.getBirthday
import com.sbrati.rastibot.service.BirthdayReminderService
import com.sbrati.rastibot.utils.fullName
import com.sbrati.spring.boot.starter.kotlin.telegram.model.BanOptions
import com.sbrati.spring.boot.starter.kotlin.telegram.model.BotCommandSpec
import com.sbrati.spring.boot.starter.kotlin.telegram.model.BotCommands
import com.sbrati.spring.boot.starter.kotlin.telegram.model.EmptyResult
import com.sbrati.spring.boot.starter.kotlin.telegram.model.TelegramSupportedLanguages
import com.sbrati.spring.boot.starter.kotlin.telegram.model.defaults
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.answerCallbackQuery
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.removeMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.InlineKeyboardButton
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.inlineKeyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.model.startNewCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.model.then
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.TelegramGlobalOperations
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.globalOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.util.Locale
import java.util.concurrent.TimeUnit

@Configuration
open class RastiBotGlobalConfiguration {

    @Bean
    open fun botCommands(): BotCommands {
        return BotCommands(
            defaultLanguage = "en",
            commands = listOf(
                BotCommandSpec(command = "/bdreminder", descriptionKey = "command.description.bdreminder"),
                BotCommandSpec(command = "/bdreminderlist", descriptionKey = "command.description.bdreminderlist"),
                BotCommandSpec(command = "/language", descriptionKey = "command.description.language"),
                BotCommandSpec(command = "/feedback", descriptionKey = "command.description.feedback"),
            ))
    }

    @Bean
    open fun telegramSupportedLanguages(): TelegramSupportedLanguages {
        return defaults("English" to Locale("en"))
            .then("Українська" to Locale("uk"))
            .then("Русский" to Locale("ru"))
    }

    @Bean
    open fun banOptions(): BanOptions {
        return BanOptions(
            allowedRequestsPerMinute = 40,
            banDurationSeconds = TimeUnit.MINUTES.toSeconds(10),
        )
    }

    @Bean
    open fun clock(): Clock {
        return Clock.systemUTC()
    }

    @Bean
    open fun telegramGlobalOperations(
        dateHelper: DateHelper,
        birthdayReminderService: BirthdayReminderService,
    ): TelegramGlobalOperations {
        return globalOperations {
            event<Notification> { event ->
                val notification = event.payload
                val birthday = notification.getBirthday()
                val dateString = dateHelper.dateToString(notification.chatId, birthday)
                val person = notification.person
                message {
                    key = "birthdayreminder.notification.message.${notification.type.abbreviation}"
                    args = listOf(person.fullName(), person.chatId.toString(), dateString)
                    parseMode = ParseMode.MARKDOWN
                    replyView = inlineKeyboard {
                        for (action in notification.actions.orEmpty()) {
                            row(
                                InlineKeyboardButton(
                                    global = true,
                                    key = "inline.button.notification.action.${action}",
                                    callbackData = notificationAction(notification.id, action)
                                )
                            )
                        }
                    }
                }
            }
            callback<NotificationActionCallback>("notificationaction") { update, callbackData ->
                val person = birthdayReminderService.reactOnNotificationAction(update, callbackData)
                if (person != null) {
                    answerCallbackQuery {
                        callbackQueryId = update.callbackQuery?.id!!
                        key = "birthdayreminder.notification.action.result"
                        args = listOf(person.fullName())
                    }
                } else {
                    EmptyResult
                }
            }
            callback<DeleteBirthdayReminderCallbackData>(DeleteBirthdayReminderCallbackData.KEY_STRING) { update, callbackData ->
                birthdayReminderService.markAsDeleted(callbackData.reminderId)
                removeMessage(update.callbackQuery?.message?.messageId!!)
            }
            callback<ChatIdCallbackData>("replfbck") { _, _ ->
                startNewCommand("replfbck")
            }
            ban { _, options ->
                message {
                    key = "system.user.exceeded.requests.limit"
                    args = listOf(options.allowedRequestsPerMinute.toString(), options.banDurationSeconds.toString())
                }
            }
        }
    }
}