package com.sbrati.rastibot.configuration

import com.sbrati.rastibot.instruments.BirthDayHelper
import com.sbrati.rastibot.model.*
import com.sbrati.rastibot.service.BirthDayReminderService
import com.sbrati.rastibot.utils.fullName
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.component.RequestLimiter
import com.sbrati.spring.boot.starter.kotlin.telegram.model.*
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.StringCallbackData
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.stringCallback
import com.sbrati.spring.boot.starter.kotlin.telegram.model.stages.nextStage
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.TelegramGlobalOperations
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.globalOperations
import com.sbrati.spring.boot.starter.kotlin.telegram.resolver.MonthResolver
import com.sbrati.spring.boot.starter.kotlin.telegram.service.LocaleSettingsService
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import com.sbrati.spring.boot.starter.kotlin.telegram.view.TelegramView
import com.sbrati.telegram.domain.StatusCode
import me.ivmg.telegram.entities.ParseMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
open class RastiBotConfiguration {

    private val logger by LoggerDelegate()

    @Bean
    open fun requestLimiter(): RequestLimiter {
        return RequestLimiter(allowedRequestsPerMinute = 40, banDurationSeconds = 120 )
    }

    @Bean
    open fun telegramSupportedLanguages(): TelegramSupportedLanguages {
        return defaults("English" to Locale("en"))
                .then("Українська" to Locale("uk"))
                .then("Русский" to Locale("ru"))
    }

    @Bean
    open fun startCommand(): TelegramCommand<NoOpCommand> {
        return startCommand {
            stage("start") {
                start { progress ->
                    message {
                        key = "start.info.welcome.message"
                        args = listOf(progress.firstName)
                        parseMode = ParseMode.MARKDOWN
                    }
                }
            }
        }
    }

    @Bean
    open fun setLanguageCommand(view: TelegramView, localeSettingsService: LocaleSettingsService): TelegramCommand<NoOpCommand> {
        return setLanguage {
            stage("request_language") {
                start {
                    message {
                        key = "setlanguage.info.specify.language.from.the.list"
                        keyboard = keyboard { row(view.supportedLanguagesButtons()) }
                    }
                }
                text { _, text, progress ->
                    if (!localeSettingsService.isLanguageSupported(text)) {
                        message(key = "setlanguage.error.unsupported.language.specified", args = listOf(progress.firstName))
                    } else {
                        localeSettingsService.updateUserLanguagePreferences(progress.chatId!!, text)
                        finish {
                            message(key = "setlanguage.info.language.settings.changed", args = listOf(text))
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun birthDayReminderCommand(monthResolver: MonthResolver,
                                     birthDayHelper: BirthDayHelper,
                                     reminderService: BirthDayReminderService): TelegramCommand<BirthDayReminder> {

        return birthdayReminder {
            stage("request_contact") {
                start { progress ->
                    message(key = "birthdayreminder.info.specify.contact", args = listOf(progress.firstName))
                }
                contact { _, contact, progress ->
                    progress.contact = contact
                    logger.info("Received contact: $contact")
                    reminderService.checkReminderAlreadyExists(progress)
                    nextStage()
                }
            }
            stage("process_contact_validation") {
                start { noMessage() }
                event<CheckReminderExistsResult> { event, progress ->
                    val existingReminder = event.payload.existingReminder
                    if (existingReminder != null) {
                        message {
                            key = "birthdayreminder.warn.reminder.already.exists.for.contact"
                            args = listOf(birthDayHelper.dateToString(progress.chatId!!, existingReminder.birthday))
                            inlineKeyboard = inlineKeyboard {
                                row(InlineKeyboardButton(key = "inline.button.update.existing.reminder", callbackData = stringCallback("update.existing.reminder")),
                                        InlineKeyboardButton(key = "inline.button.cancel.update.existing.reminder", callbackData = stringCallback("confirm.existing.reminder")))
                            }
                        }
                    } else {
                        nextStage()
                    }
                }
                callback<StringCallbackData>("update.existing.reminder") { _, _, progress ->
                    progress.overrideExisting = true
                    nextStage()
                }
                callback<StringCallbackData>("confirm.existing.reminder") { _, _, _ ->
                    finish {
                        message(key = "birthdayreminder.info.reminder.for.contact.left.unchanged")
                    }
                }
            }
            stage("request_month") {
                start {
                    message {
                        key = "birthdayreminder.info.specify.month"
                        keyboard = months()
                    }
                }
                text { _, text, progress ->
                    val month = monthResolver.resolve(text, progress.chatId!!)
                    if (month == null) {
                        message(key = "birthdayreminder.error.month.cannot.be.resolved", args = listOf(text))
                    } else {
                        progress.month = month
                        nextStage()
                    }
                }
            }
            stage("request_day") {
                start {
                    message {
                        key = "birthdayreminder.info.specify.day"
                    }
                }
                text { _, text, progress ->
                    val day = text.toIntOrNull()
                    if (day == null) {
                        message(key = "birthdayreminder.error.day.cannot.be.resolved", args = listOf(text))
                    } else if (day <= 0 || day > progress.month.length(true)) {
                        message(key = "birthdayreminder.error.day.does.not.belong.to.month")
                    } else {
                        progress.day = day
                        nextStage()
                    }
                }
            }
            stage("request_year") {
                start {
                    message {
                        key = "birthdayreminder.info.specify.year"
                        keyboard = keyboard {
                            row(KeyboardButton(key = "birthdayreminder.skip.the.year"))
                        }
                    }
                }
                text { _, text, progress ->
                    progress.setYearFromText(text)
                    reminderService.createReminder(progress)
                    nextStage()
                }
            }
            stage("check_reminder_creation") {
                event<CreateReminderResult> { event, progress ->
                    if (event.statusCode != StatusCode.SUCCESS) {
                        finish {
                            message(key = event.statusMessage, args = listOf(progress.contact!!.firstName))
                        }
                    } else {
                        finish {
                            message {
                                key = "birthdayreminder.info.reminder.created"
                                args = listOf(progress.contact!!.fullName(), birthDayHelper.dateToString(progress.chatId!!, progress.getBirthday()))
                            }
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun telegramGlobalOperations(birthDayHelper: BirthDayHelper,
                                      reminderService: BirthDayReminderService): TelegramGlobalOperations {
        return globalOperations {
            event<Notification> { event ->
                val notification = event.payload
                val birthday = notification.getBirthday()
                message {
                    key = "birthdayreminder.notification.message.${notification.type.abbreviation}"
                    args = listOf(notification.person.fullName(), birthDayHelper.dateToString(notification.chatId, birthday))
                    parseMode = ParseMode.MARKDOWN
                    inlineKeyboard = inlineKeyboard {
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

    private fun startCommand(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("start") {
            override fun createProgressEntity(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun setLanguage(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("setlanguage") {
            override fun createProgressEntity(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun birthdayReminder(operations: TelegramCommand<BirthDayReminder>.() -> Unit): TelegramCommand<BirthDayReminder> {
        return object : TelegramCommand<BirthDayReminder>("birthdayreminder") {
            override fun createProgressEntity(): BirthDayReminder {
                return BirthDayReminder()
            }
        }.apply(operations)
    }
}