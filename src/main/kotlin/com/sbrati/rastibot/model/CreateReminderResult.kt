package com.sbrati.rastibot.model

class CreateReminderResult {

    var nextBirthdayTimestamp: Long? = null

    override fun toString(): String {
        return "CreateReminderResult(nextBirthdayTimestamp=$nextBirthdayTimestamp)"
    }
}