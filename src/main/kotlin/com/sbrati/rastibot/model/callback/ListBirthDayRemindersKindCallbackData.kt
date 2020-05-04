package com.sbrati.rastibot.model.callback

import com.sbrati.rastibot.model.BirthdayReminderListKind
import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.CallbackDataObject

class ListBirthDayRemindersKindCallbackData : CallbackDataObject() {

    companion object {
        const val KEY_STRING = "lstrmndrsknd"
    }

    lateinit var kind: BirthdayReminderListKind

    override fun asString(): String {
        return "${getKey()}|$kind"
    }

    override fun construct(value: String) {
        this.kind = BirthdayReminderListKind.valueOf(value.split("|")[1])
    }

    override fun getKey(): String {
        return KEY_STRING
    }
}

fun bdayKind(kind: BirthdayReminderListKind): ListBirthDayRemindersKindCallbackData {
    val data = ListBirthDayRemindersKindCallbackData()
    data.kind = kind
    return data
}