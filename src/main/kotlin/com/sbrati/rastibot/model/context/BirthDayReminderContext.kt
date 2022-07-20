package com.sbrati.rastibot.model.context

import com.sbrati.rastibot.model.Birthday
import com.sbrati.rastibot.model.CreateBirthdayReminderRequest
import com.sbrati.rastibot.model.Person
import com.sbrati.spring.boot.starter.kotlin.telegram.command.Context
import java.time.Month
import java.time.Year

class BirthDayReminderContext : Context() {

    var person: Person? = null
    lateinit var month: Month
    var day: Int? = null
    var year: Int? = null
    var overrideExisting: Boolean = false

    override fun toString(): String {
        return "BirthDayReminder(person=$person, month=$month, day=$day, year=$year)"
    }
}

fun BirthDayReminderContext.setYearFromText(text: String) {
    val year = text.toIntOrNull()
    if (year != null && year < Year.now().value && year > 1900) {
        this.year = year
    }
}

fun BirthDayReminderContext.getBirthday(): Birthday {
    return Birthday(this.day!!, this.month, this.year)
}

fun BirthDayReminderContext.toBirthdayReminder(): CreateBirthdayReminderRequest {
    val birthday = this.getBirthday()
    val person = this.person!!
    return CreateBirthdayReminderRequest(
        this.chatId!!,
        person,
        birthday,
    )
}