package com.sbrati.rastibot.processor

import com.sbrati.rastibot.service.BirthdayReminderProcessingService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class ScheduledProcessor(
    private val birthDayReminderProcessingService: BirthdayReminderProcessingService,
) {

    @Scheduled(cron = "\${rastibot.birthday-reminder-service.schedule.cron}", zone = "\${rastibot.birthday-reminder-service.schedule.zone}")
    fun processBirthDayReminders() {
        birthDayReminderProcessingService.processBirthDayReminders()
    }
}
