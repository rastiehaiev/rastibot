package com.sbrati.rastibot.model

class ListBirthdayRemindersResult {

    var results: List<BirthdayReminder> = emptyList()

    override fun toString(): String {
        return "ListBirthdayRemindersResult(results=$results)"
    }
}