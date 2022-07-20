package com.sbrati.rastibot.command

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.rastibot.instruments.DateHelper
import com.sbrati.rastibot.model.context.BirthDayReminderContext
import com.sbrati.rastibot.model.context.getBirthday
import com.sbrati.rastibot.model.context.setYearFromText
import com.sbrati.rastibot.model.context.toBirthdayReminder
import com.sbrati.rastibot.service.BirthdayReminderService
import com.sbrati.rastibot.service.SpaceNotificationService
import com.sbrati.rastibot.utils.asPerson
import com.sbrati.rastibot.utils.fullName
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.model.MultipleResults
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.StringCallbackData
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.stringCallback
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.EmptyMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.removeMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.InlineKeyboardButton
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.KeyboardButton
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.NoReplyView
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.days
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.inlineKeyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.keyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.months
import com.sbrati.spring.boot.starter.kotlin.telegram.model.stages.nextStage
import com.sbrati.spring.boot.starter.kotlin.telegram.resolver.MonthResolver
import org.springframework.stereotype.Component

@Component
open class BirthdayReminder(
    private val monthResolver: MonthResolver,
    private val dateHelper: DateHelper,
    private val birthdayReminderService: BirthdayReminderService,
    private val spaceNotificationService: SpaceNotificationService,
): TelegramCommand<BirthDayReminderContext>("bdreminder", BirthDayReminderContext::class.java) {

    init {
        stage("request_person") {
            start { _, context ->
                message(key = "birthdayreminder.info.specify.contact", args = listOf(context.firstName))
            }
            fun checkReminderExistsAndProceed(context: BirthDayReminderContext): Any {
                val birthday = birthdayReminderService.findExisting(context.chatId!!, context.person!!) ?: return nextStage()
                return message {
                    key = "birthdayreminder.warn.reminder.already.exists.for.contact"
                    args = listOf(dateHelper.dateToString(context.chatId!!, birthday))
                    replyView = inlineKeyboard {
                        row(
                            InlineKeyboardButton(key = "inline.button.update.existing.reminder", callbackData = stringCallback("update.existing.reminder")),
                            InlineKeyboardButton(key = "inline.button.cancel.update.existing.reminder", callbackData = stringCallback("confirm.existing.reminder"))
                        )
                    }
                }
            }
            contact { _, contact, context ->
                context.person = contact.asPerson()
                checkReminderExistsAndProceed(context)
            }
            forwardedUser { _, forwardedMessage, context ->
                val user = forwardedMessage.from
                if (user != null) {
                    if (user.isBot) {
                        message { key = "birthdayreminder.error.unable.to.create.reminder.for.bot" }
                    } else {
                        context.person = user.asPerson()
                        checkReminderExistsAndProceed(context)
                    }
                } else {
                    val senderName = forwardedMessage.senderName
                    if (senderName != null) {
                        val person = senderName.asPerson()
                        context.person = person
                        checkReminderExistsAndProceed(context)
                    } else {
                        EmptyMessage
                    }
                }
            }
            text { _, text, context ->
                context.person = text.asPerson()
                checkReminderExistsAndProceed(context)
            }
            callback<StringCallbackData>("update.existing.reminder") { update, _, context ->
                context.overrideExisting = true
                nextStage()
            }
            callback<StringCallbackData>("confirm.existing.reminder") { update, _, _ ->
                finish {
                    MultipleResults().also {
                        it += removeMessage(update.callbackQuery?.message?.messageId!!)
                        it += message(key = "birthdayreminder.info.reminder.for.contact.left.unchanged")
                    }
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
                    replyView = days()
                }
            }
            text { _, text, context ->
                val day = text.toIntOrNull()
                if (day == null) {
                    message {
                        key = "birthdayreminder.error.day.cannot.be.resolved"
                        args = listOf(text)
                        replyView = NoReplyView
                    }
                } else if (day <= 0 || day > context.month.length(true)) {
                    message {
                        key = "birthdayreminder.error.day.does.not.belong.to.month"
                        replyView = NoReplyView
                    }
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
                val birthdayReminder = context.toBirthdayReminder()
                birthdayReminderService.create(birthdayReminder)

                val personChatId = context.person!!.chatId
                val userLink = if (personChatId != null) {
                    "[${context.person!!.fullName()}](tg://user?id=${personChatId})"
                } else {
                    context.person!!.fullName()
                }

                if (!context.overrideExisting) {
                    val date = dateHelper.dateToString(birthdayReminder.chatId, birthdayReminder.birthday)
                    spaceNotificationService.onNewBirthdayReminder(birthdayReminder.chatId, date, birthdayReminder.person)
                }

                finish {
                    message {
                        key = "birthdayreminder.info.reminder.created"
                        parseMode = ParseMode.MARKDOWN
                        args = listOf(userLink, dateHelper.dateToString(context.chatId!!, context.getBirthday()))
                    }
                }
            }
        }
    }
}
