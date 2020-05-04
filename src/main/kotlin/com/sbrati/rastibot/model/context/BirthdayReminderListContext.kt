package com.sbrati.rastibot.model.context

import com.sbrati.rastibot.model.BirthdayReminderListKind
import com.sbrati.spring.boot.starter.kotlin.telegram.command.Context

class BirthdayReminderListContext : Context() {

    var messageId: Long = 0
    lateinit var kind: BirthdayReminderListKind
}