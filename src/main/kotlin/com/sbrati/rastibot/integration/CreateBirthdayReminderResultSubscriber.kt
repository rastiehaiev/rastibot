package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.CreateReminderResult
import com.sbrati.spring.boot.starter.kotlin.telegram.gcp.pubsub.GcpPubSubTelegramSubscriber
import org.springframework.stereotype.Component

@Component
class CreateBirthdayReminderResultSubscriber
    : GcpPubSubTelegramSubscriber<CreateReminderResult>(CreateReminderResult::class.java, "create-birthday-reminder-result")