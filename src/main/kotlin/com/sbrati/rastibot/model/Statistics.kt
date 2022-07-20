package com.sbrati.rastibot.model

data class Statistics(
    val userServiceUp: Boolean,
    val usersCountTotal: Long?,
    val usersCountActive: Long?,
    val reminderServiceUp: Boolean,
    val remindersCountTotal: Long?,
    val remindersCountActive: Long?
)