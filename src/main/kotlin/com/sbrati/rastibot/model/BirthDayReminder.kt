package com.sbrati.rastibot.model

import com.sbrati.spring.boot.starter.kotlin.telegram.command.Context
import me.ivmg.telegram.entities.Contact
import java.time.Month
import java.time.Year

class BirthDayReminder : Context() {

    var contact: Contact? = null
    lateinit var month: Month
    var day: Int? = null
    var year: Int? = null
    var overrideExisting: Boolean = false

    override fun toString(): String {
        return "BirthDayReminder(contact=$contact, month=$month, day=$day, year=$year)"
    }
}

fun BirthDayReminder.setYearFromText(text: String) {
    val year = text.toIntOrNull()
    if (year != null && year < Year.now().value && year > 1900) {
        this.year = year
    }
}

fun BirthDayReminder.getBirthday(): Birthday {
    return Birthday(this.day!!, this.month, this.year)
}