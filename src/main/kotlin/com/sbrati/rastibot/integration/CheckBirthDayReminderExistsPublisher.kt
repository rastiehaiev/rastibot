package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.CheckReminderExistsRequest
import com.sbrati.spring.boot.starter.gcp.pubsub.GcpPubSubPublisher
import org.springframework.stereotype.Component

@Component
class CheckBirthDayReminderExistsPublisher : GcpPubSubPublisher<CheckReminderExistsRequest>("check-birthday-reminder-exists")