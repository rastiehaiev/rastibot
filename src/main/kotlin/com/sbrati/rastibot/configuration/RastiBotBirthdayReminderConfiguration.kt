package com.sbrati.rastibot.configuration

import com.sbrati.rastibot.instruments.DateHelper
import com.sbrati.rastibot.model.*
import com.sbrati.rastibot.model.BirthdayReminderListKind.*
import com.sbrati.rastibot.model.callback.ListBirthDayRemindersKindCallbackData
import com.sbrati.rastibot.model.callback.addNote
import com.sbrati.rastibot.model.callback.bdayKind
import com.sbrati.rastibot.model.callback.deleteReminder
import com.sbrati.rastibot.model.context.BirthDayReminderContext
import com.sbrati.rastibot.model.context.BirthdayReminderListContext
import com.sbrati.rastibot.model.context.getBirthday
import com.sbrati.rastibot.model.context.setYearFromText
import com.sbrati.rastibot.service.BirthDayReminderService
import com.sbrati.rastibot.utils.asPerson
import com.sbrati.rastibot.utils.fullName
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.model.MultipleResults
import com.sbrati.spring.boot.starter.kotlin.telegram.model.addTo
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.StringCallbackData
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.stringCallback
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.EmptyMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.PlainMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.plainMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.removeMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.*
import com.sbrati.spring.boot.starter.kotlin.telegram.model.stages.nextStage
import com.sbrati.spring.boot.starter.kotlin.telegram.resolver.MonthResolver
import com.sbrati.telegram.domain.StatusCode
import me.ivmg.telegram.entities.ParseMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Month

@Configuration
open class RastiBotBirthdayReminderConfiguration(
        private val monthResolver: MonthResolver,
        private val dateHelper: DateHelper,
        private val reminderService: BirthDayReminderService) {

    @Bean
    open fun commandBirthDayReminder(): TelegramCommand<BirthDayReminderContext> {
        return birthdayReminder {
            stage("request_person") {
                start { _, context ->
                    message(key = "birthdayreminder.info.specify.contact", args = listOf(context.firstName))
                }
                contact { _, contact, context ->
                    if (contact.userId == null) {
                        finish { message { key = "birthdayreminder.error.unable.to.create.reminder.for.contact.without.id" } }
                    } else {
                        context.person = contact.asPerson()
                        reminderService.checkReminderAlreadyExists(context)
                        nextStage()
                    }
                }
                forwardedUser { _, user, context ->
                    if (user.isBot) {
                        message { key = "birthdayreminder.error.unable.to.create.reminder.for.bot" }
                    } else {
                        context.person = user.asPerson()
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
                            args = listOf(dateHelper.dateToString(context.chatId!!, existingReminder.birthday))
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
                    reminderService.createReminder(context)
                    nextStage()
                }
            }
            stage("check_reminder_creation") {
                start { _, _ -> EmptyMessage }
                event<CreateReminderResult> { event, context ->
                    if (event.statusCode != StatusCode.SUCCESS) {
                        finish {
                            message(key = event.statusMessage, args = listOf(context.person!!.firstName))
                        }
                    } else {
                        finish {
                            message {
                                key = "birthdayreminder.info.reminder.created"
                                parseMode = ParseMode.MARKDOWN
                                args = listOf(context.person!!.fullName(), context.person!!.chatId.toString(), dateHelper.dateToString(context.chatId!!, context.getBirthday()))
                            }
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun commandListBirthdayReminders(): TelegramCommand<BirthdayReminderListContext> {
        return listBirthdayReminders {
            stage("choose_kind") {
                start { _, _ ->
                    message {
                        key = "birthdayreminder.list.info.choose.reminders.kind"
                        replyView = chooseRemindersKindKeyboard()
                    }
                }
                callback<ListBirthDayRemindersKindCallbackData>(ListBirthDayRemindersKindCallbackData.KEY_STRING) { update, callback, context ->
                    context.messageId = update.callbackQuery?.message?.messageId!!
                    context.kind = callback.kind
                    reminderService.sendListRemindersRequest(context.chatId!!, callback.kind)
                }
                event<ListBirthdayRemindersResult> { event, context ->
                    val month = context.kind.month()
                    val reminders = event.payload.results
                    val removeMessage = removeMessage(context.messageId)
                    val results = MultipleResults()
                    if (reminders.isNotEmpty()) {
                        // create message for each reminder
                        generateMessages(reminders, context).forEach { it.addTo(results) }
                        // finish with no message
                        finish { EmptyMessage }.addTo(results)
                        // delete message with inline buttons
                        removeMessage.addTo(results)
                    } else {
                        if (month != null) {
                            // print no reminders found in month
                            message {
                                key = "birthdayreminder.list.warn.no.reminders.found.in.month"
                                args = listOf(dateHelper.monthToString(context.chatId!!, month))
                            }.addTo(results)
                        } else {
                            // print no reminders found
                            message {
                                key = "birthdayreminder.list.warn.no.reminders.found"
                            }.addTo(results)
                            // delete message with inline buttons
                            removeMessage.addTo(results)
                        }
                    }
                    // return results
                    results
                }
            }
        }
    }

    private fun generateMessages(reminders: List<BirthdayReminder>, context: BirthdayReminderListContext): List<PlainMessage> {
        return reminders.map { reminder ->
            plainMessage {
                text = generateReminderText(reminder, context)
                parseMode = ParseMode.MARKDOWN
                replyView = inlineKeyboard {
                    row(
                            InlineKeyboardButton(key = "birthdayreminder.list.inline.button.delete.reminder", callbackData = deleteReminder(reminder.id), global = true),
                            InlineKeyboardButton(key = "birthdayreminder.list.inline.button.add.reminder.note", callbackData = addNote(reminder.id), global = true)
                    )
                }
            }
        }
    }

    private fun generateReminderText(reminder: BirthdayReminder, context: BirthdayReminderListContext): String {
        val person = reminder.person
        val dateAsString = dateHelper.dateToString(context.chatId!!, reminder.birthday)
        val userLink = "[${person.fullName()}](tg://user?id=${person.chatId})"
        return "*${dateAsString}*\n${userLink}"
    }

    private fun chooseRemindersKindKeyboard(): InlineKeyboard {
        return inlineKeyboard {
            row(InlineKeyboardButton(key = "birthdayreminder.list.inline.button.upcoming", callbackData = bdayKind(UPCOMING)))
            row(InlineKeyboardButton(key = "birthdayreminder.list.inline.button.next.three", callbackData = bdayKind(NEXT_THREE)))
            row(
                    InlineKeyboardButton(key = "month.january", callbackData = bdayKind(JANUARY)),
                    InlineKeyboardButton(key = "month.february", callbackData = bdayKind(FEBRUARY)),
                    InlineKeyboardButton(key = "month.march", callbackData = bdayKind(MARCH))
            )
            row(
                    InlineKeyboardButton(key = "month.april", callbackData = bdayKind(APRIL)),
                    InlineKeyboardButton(key = "month.may", callbackData = bdayKind(MAY)),
                    InlineKeyboardButton(key = "month.june", callbackData = bdayKind(JUNE))
            )
            row(
                    InlineKeyboardButton(key = "month.july", callbackData = bdayKind(JULY)),
                    InlineKeyboardButton(key = "month.august", callbackData = bdayKind(AUGUST)),
                    InlineKeyboardButton(key = "month.september", callbackData = bdayKind(SEPTEMBER))
            )
            row(
                    InlineKeyboardButton(key = "month.october", callbackData = bdayKind(OCTOBER)),
                    InlineKeyboardButton(key = "month.november", callbackData = bdayKind(NOVEMBER)),
                    InlineKeyboardButton(key = "month.december", callbackData = bdayKind(DECEMBER))
            )
            row(InlineKeyboardButton(key = "birthdayreminder.list.inline.button.show.all", callbackData = bdayKind(ALL)))
        }
    }

    private fun birthdayReminder(operations: TelegramCommand<BirthDayReminderContext>.() -> Unit): TelegramCommand<BirthDayReminderContext> {
        return object : TelegramCommand<BirthDayReminderContext>("bdreminder") {
            override fun createContext(): BirthDayReminderContext {
                return BirthDayReminderContext()
            }
        }.apply(operations)
    }

    private fun listBirthdayReminders(operations: TelegramCommand<BirthdayReminderListContext>.() -> Unit): TelegramCommand<BirthdayReminderListContext> {
        return object : TelegramCommand<BirthdayReminderListContext>("bdreminderlist") {
            override fun createContext(): BirthdayReminderListContext {
                return BirthdayReminderListContext()
            }
        }.apply(operations)
    }

    private fun BirthdayReminderListKind.month(): Month? {
        return Month.values().firstOrNull { it.name == this.name }
    }
}