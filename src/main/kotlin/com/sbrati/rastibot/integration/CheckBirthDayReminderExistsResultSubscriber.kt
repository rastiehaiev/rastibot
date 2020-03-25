package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.CheckReminderExistsResult
import com.sbrati.spring.boot.starter.kotlin.telegram.gcp.pubsub.GcpPubSubTelegramSubscriber
import org.springframework.stereotype.Component

@Component
class CheckBirthDayReminderExistsResultSubscriber
    : GcpPubSubTelegramSubscriber<CheckReminderExistsResult>(CheckReminderExistsResult::class.java, "check-birthday-reminder-exists-result")