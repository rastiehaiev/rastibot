package com.sbrati.rastibot.configuration

import com.sbrati.rastibot.instruments.BirthDayHelper
import com.sbrati.rastibot.model.*
import com.sbrati.rastibot.properties.RastiBotProperties
import com.sbrati.rastibot.service.BirthDayReminderService
import com.sbrati.rastibot.service.StatisticsService
import com.sbrati.rastibot.utils.fullName
import com.sbrati.rastibot.utils.orUnknown
import com.sbrati.rastibot.utils.status
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.component.RequestLimiter
import com.sbrati.spring.boot.starter.kotlin.telegram.model.*
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.StringCallbackData
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.stringCallback
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.EmptyMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.answerCallbackQuery
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.forwardedMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.*
import com.sbrati.spring.boot.starter.kotlin.telegram.model.stages.nextStage
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.TelegramGlobalOperations
import com.sbrati.spring.boot.starter.kotlin.telegram.operations.globalOperations
import com.sbrati.spring.boot.starter.kotlin.telegram.resolver.MonthResolver
import com.sbrati.spring.boot.starter.kotlin.telegram.service.LocaleSettingsService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.UserAwarenessService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.userAwarenessService
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import com.sbrati.spring.boot.starter.kotlin.telegram.util.chatId
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
    open fun commandBirthDayReminder(monthResolver: MonthResolver, birthDayHelper: BirthDayHelper, reminderService: BirthDayReminderService): TelegramCommand<BirthDayReminder> {
        return birthdayReminder {
            stage("request_contact") {
                start { _, context ->
                    message(key = "birthdayreminder.info.specify.contact", args = listOf(context.firstName))
                }
                contact { _, contact, context ->
                    if (contact.userId == null) {
                        finish { message { key = "birthdayreminder.error.unable.to.create.reminder.for.contact.without.id" } }
                    } else {
                        context.contact = contact
                        logger.debug("Received contact: $contact")
                        reminderService.checkReminderAlreadyExists(context)
                        nextStage()
                    }
                }
            }
            stage("process_contact_validation") {
                start { _, _ -> EmptyMessage }
                event<CheckReminderExistsResult> { event, context ->
                    val existingReminder = event.payload.existingReminder
                    if (existingReminder != null) {
                        message {
                            key = "birthdayreminder.warn.reminder.already.exists.for.contact"
                            args = listOf(birthDayHelper.dateToString(context.chatId!!, existingReminder.birthday))
                            replyView = inlineKeyboard {
                                row(InlineKeyboardButton(key = "inline.button.update.existing.reminder", callbackData = stringCallback("update.existing.reminder")),
                                        InlineKeyboardButton(key = "inline.button.cancel.update.existing.reminder", callbackData = stringCallback("confirm.existing.reminder")))
                            }
                        }
                    } else {
                        nextStage()
                    }
                }
                callback<StringCallbackData>("update.existing.reminder") { _, _, context ->
                    context.overrideExisting = true
                    nextStage()
                }
                callback<StringCallbackData>("confirm.existing.reminder") { _, _, _ ->
                    finish {
                        message(key = "birthdayreminder.info.reminder.for.contact.left.unchanged")
                    }
                }
            }
            stage("request_month") {
                start { _, _ ->
                    message {
                        key = "birthdayreminder.info.specify.month"
                        replyView = months()
                    }
                }
                text { _, text, context ->
                    val month = monthResolver.resolve(text, context.chatId!!)
                    if (month == null) {
                        message {
                            key = "birthdayreminder.error.month.cannot.be.resolved"
                            args = listOf(text)
                            replyView = NoReplyView
                        }
                    } else {
                        context.month = month
                        nextStage()
                    }
                }
            }
            stage("request_day") {
                start { _, _ ->
                    message {
                        key = "birthdayreminder.info.specify.day"
                    }
                }
                text { _, text, context ->
                    val day = text.toIntOrNull()
                    if (day == null) {
                        message(key = "birthdayreminder.error.day.cannot.be.resolved", args = listOf(text))
                    } else if (day <= 0 || day > context.month.length(true)) {
                        message(key = "birthdayreminder.error.day.does.not.belong.to.month")
                    } else {
                        context.day = day
                        nextStage()
                    }
                }
            }
            stage("request_year") {
                start { _, _ ->
                    message {
                        key = "birthdayreminder.info.specify.year"
                        replyView = keyboard {
                            row(KeyboardButton(key = "birthdayreminder.skip.the.year"))
                        }
                    }
                }
                text { _, text, context ->
                    context.setYearFromText(text)
                    reminderService.createReminder(context)
                    nextStage()
                }
            }
            stage("check_reminder_creation") {
                event<CreateReminderResult> { event, context ->
                    if (event.statusCode != StatusCode.SUCCESS) {
                        finish {
                            message(key = event.statusMessage, args = listOf(context.contact!!.firstName))
                        }
                    } else {
                        finish {
                            message {
                                key = "birthdayreminder.info.reminder.created"
                                args = listOf(context.contact!!.fullName(), birthDayHelper.dateToString(context.chatId!!, context.getBirthday()))
                            }
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
    open fun commandNotifyAll(): TelegramCommand<NoOpCommand> {
        return notifyAll {
            stage("notifyall") {
                start { _, _ ->
                    message { key = "notifyall.info.compose.message.to.all.users" }
                }
                update { update, _ ->
                    finish {
                        route {
                            sender {
                                message { key = "notifyall.info.message.has.been.sent" }
                            }
                            everyone {
                                forwardedMessage {
                                    parseMode = ParseMode.MARKDOWN
                                    original = update
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun commandStats(statisticsService: StatisticsService): TelegramCommand<NoOpCommand> {
        return stats {
            stage("stats") {
                start { _, _ ->
                    val statistics = statisticsService.getStatistics()
                    finish {
                        message {
                            key = "stats.info.display.statistics.information"
                            args = listOf(
                                    statistics.userServiceUp.status(),
                                    statistics.usersCount.orUnknown(),
                                    statistics.reminderServiceUp.status(),
                                    statistics.remindersCount.orUnknown())
                            parseMode = ParseMode.MARKDOWN
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun commandReplyFeedback(): TelegramCommand<ReplyFeedback> {
        return replyFeedback {
            stage("compose") {
                start { update, context ->
                    context.receiverChatId = update?.getChatIdFromCallbackData()
                    message {
                        key = "replyfeedback.compose.message.to.user"
                    }
                }
                update { update, context ->
                    finish {
                        route {
                            val receiverChatId = context.receiverChatId
                            receiverChatId?.let {
                                sender {
                                    message {
                                        key = "replyfeedback.message.has.been.sent"
                                        parseMode = ParseMode.MARKDOWN
                                    }
                                }
                                receiver(receiverChatId) {
                                    forwardedMessage {
                                        original = update
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
    open fun telegramGlobalOperations(birthDayHelper: BirthDayHelper, reminderService: BirthDayReminderService): TelegramGlobalOperations {
        return globalOperations {
            event<Notification> { event ->
                val notification = event.payload
                val birthday = notification.getBirthday()
                val dateString = birthDayHelper.dateToString(notification.chatId, birthday)
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

    private fun birthdayReminder(operations: TelegramCommand<BirthDayReminder>.() -> Unit): TelegramCommand<BirthDayReminder> {
        return object : TelegramCommand<BirthDayReminder>("birthdayreminder") {
            override fun createContext(): BirthDayReminder {
                return BirthDayReminder()
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

    private fun notifyAll(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("notifyall", admin = true) {
            override fun createContext(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun stats(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("stats", admin = true) {
            override fun createContext(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun replyFeedback(operations: TelegramCommand<ReplyFeedback>.() -> Unit): TelegramCommand<ReplyFeedback> {
        return object : TelegramCommand<ReplyFeedback>("replfbck", admin = true, synthetic = true) {
            override fun createContext(): ReplyFeedback {
                return ReplyFeedback()
            }
        }.apply(operations)
    }
}