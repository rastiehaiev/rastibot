package com.sbrati.rastibot.service

import com.sbrati.rastibot.model.Notification
import com.sbrati.rastibot.processor.BirthDayReminderProcessor
import com.sbrati.rastibot.properties.RastiBotBirthdayReminderServiceScheduleProperties
import com.sbrati.spring.boot.starter.kotlin.telegram.component.SyntheticEventSender
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class BirthdayReminderProcessingService(
    private val syntheticEventSender: SyntheticEventSender,
    private val eventCreationService: EventCreationService,
    private val birthdayReminderService: BirthdayReminderService,
    private val birthDayReminderProcessor: BirthDayReminderProcessor,
    private val properties: RastiBotBirthdayReminderServiceScheduleProperties,
) {

    private val log by LoggerDelegate()

    @Transactional
    open fun processBirthDayReminders() {
        log.info("Started processing birthday reminders.")
        var nextBatchAvailable = true
        var currentBatch = 1
        while (nextBatchAvailable) {
            val notifications: List<Notification> = birthDayReminderProcessor.processBatch(properties.batchSize)
            if (notifications.isEmpty()) {
                nextBatchAvailable = false
            } else {
                log.info("Batch number: {}.", currentBatch)
                notifications.forEach {
                    syntheticEventSender.send(eventCreationService.createGlobal(it.chatId, it))
                }
                birthdayReminderService.postProcessNotifications(notifications)
                currentBatch++
            }
        }
        log.info("Processing of birthday reminders has finished.")
    }
}
