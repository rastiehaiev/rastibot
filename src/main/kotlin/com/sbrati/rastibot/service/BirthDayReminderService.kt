package com.sbrati.rastibot.service

import com.sbrati.rastibot.integration.BirthdayReminderNotificationActionPublisher
import com.sbrati.rastibot.integration.CheckBirthDayReminderExistsPublisher
import com.sbrati.rastibot.integration.CreateBirthDayReminderPublisher
import com.sbrati.rastibot.model.*
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import com.sbrati.spring.boot.starter.kotlin.telegram.util.chatId
import com.sbrati.telegram.domain.Event
import me.ivmg.telegram.entities.Update
import org.springframework.stereotype.Service

@Service
class BirthDayReminderService(private val checkBirthDayReminderExistsPublisher: CheckBirthDayReminderExistsPublisher,
                              private val createBirthDayReminderPublisher: CreateBirthDayReminderPublisher,
                              private val birthdayReminderNotificationActionPublisher: BirthdayReminderNotificationActionPublisher) {

    private val logger by LoggerDelegate()

    fun checkReminderAlreadyExists(reminder: BirthDayReminder) {
        val request = CheckReminderExistsRequest(reminder.chatId!!, reminder.contact!!.userId!!)
        val event = createEvent(reminder.chatId!!, request)
        logger.info("Checking if reminder exists: ${request}.")
        checkBirthDayReminderExistsPublisher.publish(event)
    }

    fun createReminder(reminder: BirthDayReminder) {
        val chatId = reminder.chatId!!
        val contact = reminder.contact!!
        val person = Person(contact.userId!!, contact.firstName, contact.lastName)
        val birthday = Birthday(reminder.day!!, reminder.month, reminder.year)
        val request = CreateReminderRequest(chatId, person, birthday, reminder.overrideExisting)
        val event = createEvent(chatId, request)
        logger.info("Creating reminder: ${request}.")
        createBirthDayReminderPublisher.publish(event)
    }

    fun reactOnNotificationAction(update: Update, notificationActionCallback: NotificationActionCallback) {
        val notificationAction = NotificationAction()
        notificationAction.notificationId = notificationActionCallback.notificationId
        notificationAction.callbackQueryId = update.callbackQuery?.id
        notificationAction.action = notificationActionCallback.action

        logger.info("React on notification action: ${notificationAction}.")
        val event = createEvent(update.chatId()!!, notificationAction)
        birthdayReminderNotificationActionPublisher.publish(event)
    }

    private fun <P> createEvent(chatId: Long, payload: P, timestamp: Long = System.currentTimeMillis()): Event<P> {
        val event = Event<P>()
        event.chatId = chatId
        event.timestamp = timestamp
        event.payload = payload
        return event
    }
}