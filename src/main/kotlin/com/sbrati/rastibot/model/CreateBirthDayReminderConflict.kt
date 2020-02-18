package com.sbrati.rastibot.model

data class CreateBirthDayReminderConflict(private val existing: BirthDayReminder,
                                          private val requested: BirthDayReminder)