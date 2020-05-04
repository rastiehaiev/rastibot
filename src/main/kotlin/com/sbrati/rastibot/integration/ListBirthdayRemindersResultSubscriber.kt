package com.sbrati.rastibot.integration

import com.sbrati.rastibot.model.ListBirthdayRemindersResult
import com.sbrati.spring.boot.starter.kotlin.telegram.gcp.pubsub.GcpPubSubTelegramSubscriber
import org.springframework.stereotype.Component

@Component
class ListBirthdayRemindersResultSubscriber
    : GcpPubSubTelegramSubscriber<ListBirthdayRemindersResult>(ListBirthdayRemindersResult::class.java, "list-birthday-reminders-result")