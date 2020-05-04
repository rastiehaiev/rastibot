package com.sbrati.rastibot.model.context

import com.sbrati.rastibot.model.Birthday
import com.sbrati.spring.boot.starter.kotlin.telegram.command.Context
import me.ivmg.telegram.entities.Contact
import java.time.Month
import java.time.Year

class BirthDayReminderContext : Context() {

    var contact: Contact? = null
    lateinit var month: Month
    var day: Int? = null
    var year: Int? = null
    var overrideExisting: Boolean = false

    override fun toString(): String {
        return "BirthDayReminder(contact=$contact, month=$month, day=$day, year=$year)"
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