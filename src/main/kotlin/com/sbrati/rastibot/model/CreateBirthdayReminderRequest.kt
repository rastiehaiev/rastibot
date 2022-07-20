package com.sbrati.rastibot.model

data class CreateBirthdayReminderRequest(
    val chatId: Long,
    val person: Person,
    val birthday: Birthday
)