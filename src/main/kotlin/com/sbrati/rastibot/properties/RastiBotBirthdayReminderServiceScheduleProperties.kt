package com.sbrati.rastibot.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "rastibot.birthday-reminder-service.schedule")
class RastiBotBirthdayReminderServiceScheduleProperties(
    val batchSize: Int,
    val lastUpdatedReminderHours: Int? = null,
    val zone: String? = null,
)
