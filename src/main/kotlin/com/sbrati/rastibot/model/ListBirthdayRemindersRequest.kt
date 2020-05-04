package com.sbrati.rastibot.model

class ListBirthdayRemindersRequest {

    var chatId: Long = 0
    lateinit var kind: BirthdayReminderListKind

    override fun toString(): String {
        return "ListBirthdayRemindersRequest(chatId=$chatId, kind=$kind)"
    }
}

fun listBirthdayReminders(chatId: Long, kind: BirthdayReminderListKind): ListBirthdayRemindersRequest {
    val request = ListBirthdayRemindersRequest()
    request.chatId = chatId
    request.kind = kind
    return request
}