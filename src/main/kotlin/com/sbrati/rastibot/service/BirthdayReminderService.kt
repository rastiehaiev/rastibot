package com.sbrati.rastibot.service

import com.github.kotlintelegrambot.entities.Update
import com.sbrati.rastibot.entity.BirthdayReminderEntity
import com.sbrati.rastibot.instruments.NextBirthdayTimestampCalculator
import com.sbrati.rastibot.model.BirthDayReminderStrategy
import com.sbrati.rastibot.model.Birthday
import com.sbrati.rastibot.model.BirthdayReminderData
import com.sbrati.rastibot.model.BirthdayReminderListKind
import com.sbrati.rastibot.model.CreateBirthdayReminderRequest
import com.sbrati.rastibot.model.Notification
import com.sbrati.rastibot.model.NotificationActionEnum
import com.sbrati.rastibot.model.Person
import com.sbrati.rastibot.model.callback.NotificationActionCallback
import com.sbrati.rastibot.repository.BirthdayReminderRepository
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Month
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional

@Service
open class BirthdayReminderService(
    private val clock: Clock,
    private val repository: BirthdayReminderRepository,
    private val calculator: NextBirthdayTimestampCalculator,
) {

    private val log by LoggerDelegate()

    open fun findExisting(chatId: Long, person: Person): Birthday? {
        val remindedUserId = person.getUserId()
        val existingReminderEntity = repository.findByChatIdAndRemindedUserIdAndDeletedFalse(chatId, remindedUserId)
        return if (existingReminderEntity != null) getBirthday(existingReminderEntity) else null
    }

    open fun findUpcoming(batchSize: Int): List<BirthdayReminderEntity> {
        val instant = clock.instant()
        val instantAtStartOfDay = instant.truncatedTo(ChronoUnit.DAYS)
        val upcomingBirthDaysTimestamp = instantAtStartOfDay.plus((BirthDayReminderStrategy.MAX_DAYS_AMOUNT + 1).toLong(), ChronoUnit.DAYS).toEpochMilli()
        val lastUpdatedTimestamp = instant.toEpochMilli() - TimeUnit.HOURS.toMillis(1)
        val upcoming = repository.findUpcoming(upcomingBirthDaysTimestamp, lastUpdatedTimestamp, PageRequest.of(0, batchSize))
        if (upcoming.isEmpty()) {
            log.info("Found {} upcoming reminders.", upcoming.size)
        }
        return upcoming
    }

    open fun findAllOfKind(chatId: Long, kind: BirthdayReminderListKind): List<BirthdayReminderData> {
        return findAllEntitiesOfKind(chatId, kind)
            .map { toBirthdayReminderData(it) }
    }

    private fun findAllEntitiesOfKind(chatId: Long, kind: BirthdayReminderListKind): List<BirthdayReminderEntity> {
        return when (kind) {
            BirthdayReminderListKind.ALL -> {
                val pageable: Pageable = PageRequest.of(0, 10)
                repository.findAllByChatIdAndDeletedFalseOrderByNextBirthDayTimestamp(chatId, pageable)
            }
            BirthdayReminderListKind.UPCOMING -> {
                val nearest = repository.findNearest(chatId) ?: return emptyList()
                listOf(nearest)
            }
            BirthdayReminderListKind.NEXT_THREE -> repository.findThreeNearest(chatId)
            else -> {
                val month = Month.valueOf(kind.name)
                repository.findByMonth(chatId, month.value)
            }
        }
    }

    private fun toBirthdayReminderData(entity: BirthdayReminderEntity): BirthdayReminderData {
        return BirthdayReminderData(
            entity.id,
            entity.chatId,
            person = getPerson(entity),
            getBirthday(entity),
        )
    }

    open fun create(birthDayReminder: CreateBirthdayReminderRequest): Long {
        val chatId = birthDayReminder.chatId
        val remindedUserId = birthDayReminder.person.getUserId()

        var reminder = repository.findByChatIdAndRemindedUserId(chatId, remindedUserId)
        if (reminder == null) {
            reminder = BirthdayReminderEntity()
        }
        reminder.chatId = chatId
        reminder.day = birthDayReminder.birthday.day
        reminder.month = birthDayReminder.birthday.month.value
        reminder.year = birthDayReminder.birthday.year
        reminder.remindedUserId = remindedUserId
        reminder.remindedUserFirstName = birthDayReminder.person.firstName
        reminder.remindedUserLastName = birthDayReminder.person.lastName
        reminder.disabled = (false)
        reminder.deleted = (false)
        reminder.lastUpdated = (null)
        val nextBirthDayTimestamp = calculator.nextBirthdayTimestamp(birthDayReminder.birthday.month.value, birthDayReminder.birthday.day)
        reminder.nextBirthDayTimestamp = (nextBirthDayTimestamp)
        reminder.preferredStrategy = (null)
        repository.save(reminder)
        log.info("Created reminder: {}.", reminder)
        return nextBirthDayTimestamp
    }

    @Transactional
    open fun update(reminders: List<BirthdayReminderEntity>) {
        repository.saveAll(reminders)
    }

    @Transactional
    open fun reactOnNotificationAction(update: Update, notificationActionCallback: NotificationActionCallback): Person? {
        val notificationId = notificationActionCallback.notificationId
        val actionString = notificationActionCallback.action

        val action = NotificationActionEnum.from(actionString)
        if (action == null) {
            log.warn("Could not resolve action by string [{}].", actionString)
            return null
        }
        val entity = repository.findById(notificationId).orElse(null)
        if (entity == null) {
            log.warn("Failed to react no notification action. Could not find reminder by ID={}.", notificationId)
            return null
        }
        when (action) {
            NotificationActionEnum.DO_NOT_NOTIFY_ANYMORE -> {
                entity.deleted = true
                entity.disabled = false
            }
            NotificationActionEnum.DO_NOT_NOTIFY_THIS_YEAR -> {
                entity.disabled = true
                entity.deleted = false
            }
            else -> {
                val strategy = BirthDayReminderStrategy.of(action.supportedDaysBefore)
                entity.preferredStrategy = (strategy)
                entity.disabled = false
                entity.deleted = false
            }
        }
        repository.save(entity)
        return getPerson(entity)
    }

    @Transactional
    open fun markAsDeleted(reminderId: Long) {
        log.info("Deleting reminder by ID=$reminderId.")
        repository.findById(reminderId)
            .ifPresent { reminder ->
                reminder.deleted = true
                reminder.disabled = false
            }
    }

    private fun getBirthday(existingReminderEntity: BirthdayReminderEntity): Birthday {
        return existingReminderEntity.let {
            Birthday(
                it.day,
                Month.of(it.month),
                it.year,
            )
        }
    }

    open fun getPerson(existingReminder: BirthdayReminderEntity): Person {
        return Person(
            existingReminder.remindedUserId?.toLongOrNull(),
            existingReminder.remindedUserFirstName ?: "",
            existingReminder.remindedUserLastName,
        )
    }

    open fun countAll(): Long {
        return repository.count()
    }

    open fun countNotDeleted(): Long {
        return repository.countAllByDeletedFalse()
    }

    open fun postProcessNotifications(notifications: List<Notification>) {
        notifications.forEach { notification ->
            val daysAmount: Int = notification.type.daysAmount
            log.info("Update last notified days to {} for reminder with ID={}.", daysAmount, notification.id)
            repository.updateLastNotifiedDays(notification.id, daysAmount)
        }
    }
}