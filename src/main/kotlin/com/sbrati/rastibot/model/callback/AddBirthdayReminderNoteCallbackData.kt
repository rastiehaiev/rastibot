package com.sbrati.rastibot.model.callback

import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.CallbackDataObject

class AddBirthdayReminderNoteCallbackData : CallbackDataObject() {

    companion object {
        const val KEY_STRING = "ddrmndrnt"
    }

    var reminderId: Long = 0

    override fun asString(): String {
        return "${getKey()}|$reminderId"
    }

    override fun construct(value: String) {
        val keyAndReminderIdSpec = value.split("|")
        if (keyAndReminderIdSpec.size != 2) {
            throw IllegalStateException("Failed to instantiate AddBirthdayReminderNoteCallbackData from string '$value'.")
        }
        this.reminderId = keyAndReminderIdSpec[1].toLong()
    }

    override fun getKey(): String {
        return KEY_STRING
    }
}

fun addNote(id: Long): AddBirthdayReminderNoteCallbackData {
    val callbackData = AddBirthdayReminderNoteCallbackData()
    callbackData.reminderId = id
    return callbackData
}