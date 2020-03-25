package com.sbrati.rastibot.model

import java.time.Month

class Notification {

    var id: Long = 0
    var chatId: Long = 0
    lateinit var person: Person
    lateinit var type: BirthDayReminderStrategy
    var day = 0
    var month = 0
    var year: Int? = null
    var actions: List<String>? = null

    override fun toString(): String {
        return "Notification(id=$id, chatId=$chatId, person=$person, type=$type, day=$day, month=$month, year=$year, actions=$actions)"
    }
}

fun Notification.getBirthday(): Birthday {
    return Birthday(day, Month.of(month), year)
}