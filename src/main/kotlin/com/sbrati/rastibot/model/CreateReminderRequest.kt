package com.sbrati.rastibot.model

data class CreateReminderRequest(val chatId: Long,
                                 val person: Person,
                                 val birthday: Birthday,
                                 val override: Boolean)