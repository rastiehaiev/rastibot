package com.sbrati.rastibot.processor

import com.sbrati.rastibot.entity.BirthdayReminderEntity
import com.sbrati.rastibot.instruments.NextBirthdayTimestampCalculator
import com.sbrati.rastibot.instruments.TargetStrategyResolver
import com.sbrati.rastibot.model.BirthDayReminderStrategy
import com.sbrati.rastibot.model.Notification
import com.sbrati.rastibot.model.NotificationActionEnum
import com.sbrati.rastibot.service.BirthdayReminderService
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional

/**
 * This class is responsible to process upcoming reminders and generate corresponding notifications.
 * For each particular reminder, an appropriate strategy is found unless preferred one is specified.
 * A service that will consume generated notifications will have all the necessary information to send it to user in appropriate way.
 * If reminder has expired, the next birthday timestamp is calculated to be processed in the next year.
 */
@Component
open class BirthDayReminderProcessor(
    private val clock: Clock,
    private val birthdayReminderService: BirthdayReminderService,
    private val calculator: NextBirthdayTimestampCalculator,
    private val targetStrategyResolver: TargetStrategyResolver,
) {

    private val log by LoggerDelegate()

    @Transactional
    open fun processBatch(batchSize: Int): List<Notification> {
        val notifications = ArrayList<Notification>()
        val expiredReminders = ArrayList<BirthdayReminderEntity>()
        val instantAtStartOfDay = clock.instant().truncatedTo(ChronoUnit.DAYS)
        log.info("Current timestamp at start of the day: {}.", instantAtStartOfDay.toEpochMilli())
        val upcomingReminders = birthdayReminderService.findUpcoming(batchSize)
        for (reminder in upcomingReminders) {
            log.info("Processing reminder {}.", reminder)
            if (instantAtStartOfDay.toEpochMilli() > reminder.nextBirthDayTimestamp) {
                log.info("Reminder has expired: {}.", reminder)
                expiredReminders.add(reminder)
            } else {
                val notification = tryToCreateNotification(reminder)
                if (notification != null) notifications.add(notification)
            }
            reminder.lastUpdated = clock.instant().toEpochMilli()
        }
        processExpiredReminders(expiredReminders)
        birthdayReminderService.update(upcomingReminders)
        if (notifications.size != 0) {
            log.info("Created {} notifications.", notifications.size)
        }
        return notifications
    }

    private fun tryToCreateNotification(reminder: BirthdayReminderEntity): Notification? {
        if (reminder.disabled) {
            log.info("Reminder {} is disabled.", reminder)
            return null
        }
        val targetStrategy = targetStrategyResolver.resolve(reminder.nextBirthDayTimestamp)
        if (targetStrategy == null) {
            log.info("Reminder {} is not to be processed yet.", reminder)
            return null
        }
        val preferredStrategy = reminder.preferredStrategy
        if (preferredStrategy != null) {
            if (preferredStrategy.daysAmount < targetStrategy.daysAmount) {
                log.info(
                    "Reminder: {}. Preferred strategy '{}' is less than target one {}. Skipping for now.",
                    reminder, preferredStrategy, targetStrategy
                )
                return null
            }
        }
        val lastNotifiedDays = reminder.lastNotifiedDays
        if (lastNotifiedDays != null && targetStrategy.daysAmount == lastNotifiedDays) {
            log.info("Reminder {} was already sent with strategy '{}'.", reminder, targetStrategy)
            return null
        }
        return getNotificationFromReminder(reminder, targetStrategy)
    }

    private fun processExpiredReminders(expiredReminders: List<BirthdayReminderEntity>) {
        if (expiredReminders.isNotEmpty()) {
            log.info("Found {} expired reminders.", expiredReminders.size)
            for (expiredReminder in expiredReminders) {
                val nextBirthdayTimestamp: Long = calculator.nextBirthdayTimestamp(expiredReminder.month, expiredReminder.day)
                expiredReminder.nextBirthDayTimestamp = nextBirthdayTimestamp
                expiredReminder.disabled = false
                expiredReminder.lastNotifiedDays = null
            }
        }
    }

    private fun getNotificationFromReminder(
        reminder: BirthdayReminderEntity,
        targetStrategy: BirthDayReminderStrategy,
    ): Notification {
        val notification = Notification()
        notification.id = reminder.id
        notification.chatId = reminder.chatId
        notification.person = birthdayReminderService.getPerson(reminder)
        notification.type = targetStrategy
        notification.day = reminder.day
        notification.month = reminder.month
        notification.year = reminder.year
        notification.actions = getAvailableActions(targetStrategy)
        return notification
    }

    private fun getAvailableActions(strategy: BirthDayReminderStrategy): List<String> {
        return NotificationActionEnum.values()
            .filter { action -> action.supportedDaysBefore < strategy.daysAmount }
            .map { it.abbreviation }
    }
}