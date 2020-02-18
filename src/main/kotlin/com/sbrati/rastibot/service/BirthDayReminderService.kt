package com.sbrati.rastibot.service

import com.sbrati.rastibot.client.BirthDayReminderPublisher
import com.sbrati.rastibot.component.MonthResolver
import com.sbrati.rastibot.model.BirthDayReminder
import com.sbrati.rastibot.model.Person
import com.sbrati.rastibot.setContactFullName
import com.sbrati.spring.starter.telegram.dsl.Transaction
import com.sbrati.spring.starter.telegram.exception.TelegramUpdateProcessingException
import com.sbrati.spring.starter.telegram.model.key
import com.sbrati.spring.starter.telegram.utils.LoggerDelegate
import com.sbrati.spring.starter.telegram.utils.getLocale
import me.ivmg.telegram.entities.Contact
import me.ivmg.telegram.entities.Update
import org.springframework.stereotype.Service
import java.time.Month

@Service
class BirthDayReminderService(private val monthResolver: MonthResolver,
                              private val publisher: BirthDayReminderPublisher) {

    private val logger by LoggerDelegate()

    fun processContact(update: Update, transaction: Transaction) {
        val locale = update.getLocale()
        val contact = update.message?.contact
                ?: throw TelegramUpdateProcessingException(key("error.birthdayreminder.contact.should.be.provided"), locale)
        transaction.setContactFullName(contact)
        transaction.setContact(contact)
    }

    fun processMonth(update: Update, transaction: Transaction) {
        val locale = update.getLocale()
        val monthAsString = update.message?.text
                ?: throw TelegramUpdateProcessingException(key("error.birthdayreminder.month.should.be.provided"), locale)
        val month = monthResolver.resolve(monthAsString)
                ?: throw TelegramUpdateProcessingException(key("error.birthdayreminder.valid.month.should.be.provided"), locale)
        transaction.setMonth(month)
    }

    fun processDay(update: Update, transaction: Transaction) {
        val locale = update.getLocale()
        val day = update.message?.text?.toIntOrNull()
                ?: throw TelegramUpdateProcessingException(key("error.birthdayreminder.day.should.be.provided"), locale)

        val month = transaction.getMonth("month")
        val monthLength = month.length(true)
        if (day < 1 || day > monthLength) {
            throw TelegramUpdateProcessingException(key("error.birthdayreminder.valid.day.should.be.provided").arg(monthLength.toString()), locale)
        }

        val contact = transaction.getContact()

        val person = Person(contact.userId!!, contact.firstName, contact.lastName)
        val birthDayReminder = BirthDayReminder(chatId = update.message!!.chat.id,
                day = day,
                month = month.value,
                person = person)
        logger.info("Creating reminder $birthDayReminder.")
        publisher.createReminder(birthDayReminder)
    }

    private fun Transaction.setMonth(month: Month) {
        this.put("month", month)
    }

    private fun Transaction.setContact(contact: Contact) {
        this.put("contact", contact)
    }

    private fun Transaction.getContact(): Contact {
        return this.get("contact") as Contact
    }
}