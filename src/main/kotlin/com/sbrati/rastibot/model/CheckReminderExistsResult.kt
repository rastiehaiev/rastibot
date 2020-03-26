package com.sbrati.rastibot.model

class CheckReminderExistsResult {

    var existingReminder: ExistingReminder? = null

    override fun toString(): String {
        return "CheckReminderExistsResult(existingReminder=$existingReminder)"
    }
}

class ExistingReminder {

    lateinit var birthday: Birthday

    override fun toString(): String {
        return "ExistingReminder(birthday=$birthday)"
    }
}