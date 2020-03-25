package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.Notification
import com.sbrati.spring.boot.starter.kotlin.telegram.gcp.pubsub.GcpPubSubTelegramSubscriber
import org.springframework.stereotype.Component

@Component
class BirthdayReminderNotificationSubscriber
    : GcpPubSubTelegramSubscriber<Notification>(Notification::class.java, "birthday-reminder-notification")