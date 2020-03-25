package com.sbrati.rastibot.model

class CreateReminderRequest(val chatId: Long,
                            val person: Person,
                            val birthday: Birthday,
                            val override: Boolean)