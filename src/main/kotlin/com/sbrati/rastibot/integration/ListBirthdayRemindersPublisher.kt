package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.ListBirthdayRemindersRequest
import com.sbrati.spring.boot.starter.gcp.pubsub.GcpPubSubPublisher
import org.springframework.stereotype.Component

@Component
class ListBirthdayRemindersPublisher : GcpPubSubPublisher<ListBirthdayRemindersRequest>("list-birthday-reminders")