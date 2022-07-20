package com.sbrati.rastibot.command

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.rastibot.instruments.DateHelper
import com.sbrati.rastibot.model.BirthdayReminderData
import com.sbrati.rastibot.model.BirthdayReminderListKind
import com.sbrati.rastibot.model.callback.ListBirthDayRemindersKindCallbackData
import com.sbrati.rastibot.model.callback.addNote
import com.sbrati.rastibot.model.callback.bdayKind
import com.sbrati.rastibot.model.callback.deleteReminder
import com.sbrati.rastibot.model.context.BirthdayReminderListContext
import com.sbrati.rastibot.service.BirthdayReminderService
import com.sbrati.rastibot.utils.fullName
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.model.MultipleResults
import com.sbrati.spring.boot.starter.kotlin.telegram.model.addTo
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.EmptyMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.PlainMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.plainMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.removeMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.InlineKeyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.InlineKeyboardButton
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.inlineKeyboard
import org.springframework.stereotype.Component
import java.time.Month

@Component
open class BirthdayReminderList(
    private val dateHelper: DateHelper,
    private val birthdayReminderService: BirthdayReminderService,
) : TelegramCommand<BirthdayReminderListContext>(
    name = "bdreminderlist",
    contextType = BirthdayReminderListContext::class.java,
) {

    init {
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

                val reminders = birthdayReminderService.findAllOfKind(context.chatId!!, callback.kind)
                val month = context.kind.month()
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

    private fun generateMessages(reminders: List<BirthdayReminderData>, context: BirthdayReminderListContext): List<PlainMessage> {
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

    private fun generateReminderText(reminder: BirthdayReminderData, context: BirthdayReminderListContext): String {
        val person = reminder.person
        val dateAsString = dateHelper.dateToString(context.chatId!!, reminder.birthday)
        val personChatId = person.getUserId().toLongOrNull()
        val userLink = if (personChatId != null) {
            "[${person.fullName()}](tg://user?id=${person.chatId})"
        } else {
            person.fullName()
        }
        return "*${dateAsString}*\n${userLink}"
    }

    private fun chooseRemindersKindKeyboard(): InlineKeyboard {
        return inlineKeyboard {
            row(InlineKeyboardButton(key = "birthdayreminder.list.inline.button.upcoming", callbackData = bdayKind(BirthdayReminderListKind.UPCOMING)))
            row(InlineKeyboardButton(key = "birthdayreminder.list.inline.button.next.three", callbackData = bdayKind(BirthdayReminderListKind.NEXT_THREE)))
            row(
                InlineKeyboardButton(key = "month.january", callbackData = bdayKind(BirthdayReminderListKind.JANUARY)),
                InlineKeyboardButton(key = "month.february", callbackData = bdayKind(BirthdayReminderListKind.FEBRUARY)),
                InlineKeyboardButton(key = "month.march", callbackData = bdayKind(BirthdayReminderListKind.MARCH))
            )
            row(
                InlineKeyboardButton(key = "month.april", callbackData = bdayKind(BirthdayReminderListKind.APRIL)),
                InlineKeyboardButton(key = "month.may", callbackData = bdayKind(BirthdayReminderListKind.MAY)),
                InlineKeyboardButton(key = "month.june", callbackData = bdayKind(BirthdayReminderListKind.JUNE))
            )
            row(
                InlineKeyboardButton(key = "month.july", callbackData = bdayKind(BirthdayReminderListKind.JULY)),
                InlineKeyboardButton(key = "month.august", callbackData = bdayKind(BirthdayReminderListKind.AUGUST)),
                InlineKeyboardButton(key = "month.september", callbackData = bdayKind(BirthdayReminderListKind.SEPTEMBER))
            )
            row(
                InlineKeyboardButton(key = "month.october", callbackData = bdayKind(BirthdayReminderListKind.OCTOBER)),
                InlineKeyboardButton(key = "month.november", callbackData = bdayKind(BirthdayReminderListKind.NOVEMBER)),
                InlineKeyboardButton(key = "month.december", callbackData = bdayKind(BirthdayReminderListKind.DECEMBER))
            )
            row(InlineKeyboardButton(key = "birthdayreminder.list.inline.button.show.all", callbackData = bdayKind(BirthdayReminderListKind.ALL)))
        }
    }

    private fun BirthdayReminderListKind.month(): Month? = Month.values().firstOrNull { it.name == this.name }
}
