package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.NotificationActionResult
import com.sbrati.spring.boot.starter.kotlin.telegram.gcp.pubsub.GcpPubSubTelegramSubscriber
import org.springframework.stereotype.Component

@Component
class BirthdayReminderNotificationActionResultSubscriber
    : GcpPubSubTelegramSubscriber<NotificationActionResult>(NotificationActionResult::class.java, "birthday-reminder-notification-action-result")