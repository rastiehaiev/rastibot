package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.DeleteBirthdayReminderRequest
import com.sbrati.spring.boot.starter.gcp.pubsub.GcpPubSubPublisher
import org.springframework.stereotype.Component

@Component
class DeleteBirthdayReminderPublisher : GcpPubSubPublisher<DeleteBirthdayReminderRequest>("delete-birthday-reminder")