package com.sbrati.rastibot.model.callback

import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.CallbackDataObject

class DeleteBirthdayReminderCallbackData : CallbackDataObject() {

    companion object {
        const val KEY_STRING = "dltrmndr"
    }

    var reminderId: Long = 0

    override fun asString(): String {
        return "${getKey()}|$reminderId"
    }

    override fun construct(value: String) {
        val keyAndReminderIdSpec = value.split("|")
        if (keyAndReminderIdSpec.size != 2) {
            throw IllegalStateException("Failed to instantiate ReminderActionCallbackData from string '$value'.")
        }
        this.reminderId = keyAndReminderIdSpec[1].toLong()
    }

    override fun getKey(): String {
        return KEY_STRING
    }
}

fun deleteReminder(id: Long): DeleteBirthdayReminderCallbackData {
    val callbackData = DeleteBirthdayReminderCallbackData()
    callbackData.reminderId = id
    return callbackData
}