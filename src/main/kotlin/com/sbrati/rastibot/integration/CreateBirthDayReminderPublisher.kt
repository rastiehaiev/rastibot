package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.CreateReminderRequest
import com.sbrati.spring.boot.starter.gcp.pubsub.GcpPubSubPublisher
import org.springframework.stereotype.Component

@Component
class CreateBirthDayReminderPublisher : GcpPubSubPublisher<CreateReminderRequest>("create-birthday-reminder")