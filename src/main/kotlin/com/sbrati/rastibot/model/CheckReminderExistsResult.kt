package com.sbrati.rastibot.model

class CheckReminderExistsResult {

    var existingReminder: ExistingReminder? = null
}

class ExistingReminder {

    lateinit var birthday: Birthday
}