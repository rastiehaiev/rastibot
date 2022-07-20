package com.sbrati.rastibot.model

data class BirthdayReminderData(
    val id: Long,
    val chatId: Long,
    val person: Person,
    val birthday: Birthday
)