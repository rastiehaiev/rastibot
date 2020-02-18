package com.sbrati.rastibot.model

data class CreateBirthDayReminderSuccess(val nextBirthDayTimestamp: Long,
                                         val birthDayReminder: BirthDayReminder)