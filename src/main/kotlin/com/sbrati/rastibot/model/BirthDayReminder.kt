package com.sbrati.rastibot.model

data class BirthDayReminder(val chatId: Long,
                            val day: Int,
                            val month: Int,
                            val year: Int? = null,
                            val person: Person)