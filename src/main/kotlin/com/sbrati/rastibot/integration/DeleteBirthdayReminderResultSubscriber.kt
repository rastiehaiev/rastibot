package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.DeleteBirthdayReminderResult
import com.sbrati.spring.boot.starter.kotlin.telegram.gcp.pubsub.GcpPubSubTelegramSubscriber
import org.springframework.stereotype.Component

@Component
class DeleteBirthdayReminderResultSubscriber
    : GcpPubSubTelegramSubscriber<DeleteBirthdayReminderResult>(DeleteBirthdayReminderResult::class.java, "delete-birthday-reminder-result")