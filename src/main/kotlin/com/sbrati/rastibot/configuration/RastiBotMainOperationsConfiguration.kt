package com.sbrati.rastibot.configuration

import com.sbrati.rastibot.instruments.DateHelper
import com.sbrati.rastibot.model.DeleteBirthdayReminderResult
import com.sbrati.rastibot.model.Notification
import com.sbrati.rastibot.model.NotificationActionResult
import com.sbrati.rastibot.model.callback.*
import com.sbrati.rastibot.model.getBirthday
import com.sbrati.rastibot.properties.RastiBotProperties
import com.sbrati.rastibot.service.BirthDayReminderService
import com.sbrati.rastibot.utils.fullName
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.component.RequestLimiter
import com.sbrati.spring.boot.starter.kotlin.telegram.model.*
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.answerCallbackQuery
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.forwardedMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.InlineKeyboardButton
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.NoReplyView
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.inlineKeyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.keyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.TelegramGlobalOperations
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.globalOperations
import com.sbrati.spring.boot.starter.kotlin.telegram.service.LocaleSettingsService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.UserAwarenessService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.userAwarenessService
import com.sbrati.spring.boot.starter.kotlin.telegram.util.chatId
import com.sbrati.spring.boot.starter.kotlin.telegram.view.TelegramView
import me.ivmg.telegram.entities.ParseMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
open class RastiBotMainOperationsConfiguration {

    @Bean
    open fun commandStart(): TelegramCommand<NoOpCommand> {
        return start {
            stage("start") {
                start { _, context ->
                    finish {
                        message {
                            key = "start.info.welcome.message"
                            args = listOf(context.firstName)
                            parseMode = ParseMode.MARKDOWN
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun commandSetLanguage(view: TelegramView, localeSettingsService: LocaleSettingsService): TelegramCommand<NoOpCommand> {
        return setLanguage {
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

    @Bean
    open fun commandFeedback(): TelegramCommand<NoOpCommand> {
        return feedback {
            stage("feedback") {
                start { _, _ ->
                    message { key = "feedback.info.specify.your.feedback" }
                }
                update { update, _ ->
                    finish {
                        route {
                            sender {
                                message { key = "feedback.info.thanks.for.your.feedback" }
                            }
                            admin {
                                val chatId = update.chatId()
                                forwardedMessage {
                                    original = update
                                    textWrapperKey = "feedback.info.admin.message"
                                    args = listOf(update.fullName(), chatId.toString())
                                    parseMode = ParseMode.MARKDOWN
                                    replyView = chatId?.let {
                                        inlineKeyboard {
                                            row(InlineKeyboardButton(
                                                    global = true,
                                                    key = "inline.button.reply.feedback",
                                                    callbackData = chatIdCallback("replfbck", chatId)))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun telegramGlobalOperations(dateHelper: DateHelper, reminderService: BirthDayReminderService): TelegramGlobalOperations {
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
                            row(InlineKeyboardButton(
                                    global = true,
                                    key = "inline.button.notification.action.${action}",
                                    callbackData = notificationAction(notification.id, action)))
                        }
                    }
                }
            }
            callback<NotificationActionCallback>("notificationaction") { update, callbackData ->
                reminderService.reactOnNotificationAction(update, callbackData)
            }
            callback<DeleteBirthdayReminderCallbackData>(DeleteBirthdayReminderCallbackData.KEY_STRING) { update, callbackData ->
                reminderService.deleteReminder(update, callbackData)
            }
            event<DeleteBirthdayReminderResult> { event ->
                removeMessage(event.payload.messageId)
            }
            callback<ChatIdCallbackData>("replfbck") { _, _ ->
                startNewCommand("replfbck")
            }
            event<NotificationActionResult> { event ->
                answerCallbackQuery {
                    callbackQueryId = event.payload.callbackQueryId
                    key = "birthdayreminder.notification.action.result"
                    args = listOf(event.payload.person.fullName())
                }
            }
            ban { _, options ->
                message {
                    key = "system.user.exceeded.requests.limit"
                    args = listOf(options.allowedRequestsPerMinute.toString(), options.banDurationSeconds.toString())
                }
            }
        }
    }

    @Bean
    open fun requestLimiter(): RequestLimiter {
        return RequestLimiter(allowedRequestsPerMinute = 40, banDurationSeconds = 120)
    }

    @Bean
    open fun telegramSupportedLanguages(): TelegramSupportedLanguages {
        return defaults("English" to Locale("en"))
                .then("Українська" to Locale("uk"))
                .then("Русский" to Locale("ru"))
    }

    // @Bean
    open fun userAwarenessServiceSpec(properties: RastiBotProperties): UserAwarenessService {
        return userAwarenessService {
            awarenessLevel = properties.awarenessLevel
            message = message {
                key = "whatsnew.message"
                parseMode = ParseMode.MARKDOWN
            }
        }
    }

    private fun start(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("start") {
            override fun createContext(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun setLanguage(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("setlanguage") {
            override fun createContext(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun feedback(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("feedback") {
            override fun createContext(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }
}