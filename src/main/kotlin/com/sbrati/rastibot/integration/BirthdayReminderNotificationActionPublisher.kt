package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.NotificationAction
import com.sbrati.spring.boot.starter.gcp.pubsub.GcpPubSubPublisher
import org.springframework.stereotype.Component

@Component
class BirthdayReminderNotificationActionPublisher : GcpPubSubPublisher<NotificationAction>("birthday-reminder-notification-action")