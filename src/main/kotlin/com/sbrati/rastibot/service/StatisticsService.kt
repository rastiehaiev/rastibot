package com.sbrati.rastibot.service

import com.sbrati.rastibot.model.BirthdayStatistics
import com.sbrati.rastibot.model.Statistics
import com.sbrati.rastibot.model.UserStatistics
import com.sbrati.rastibot.repository.UserRepository
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import org.springframework.stereotype.Service

@Service
class StatisticsService(
    private val userRepository: UserRepository,
    private val birthdayReminderService: BirthdayReminderService,
) {

    private val logger by LoggerDelegate()

    fun getStatistics(): Statistics {
        val (userServiceUp, usersCountTotal, usersCountActive) = try {
            val stats = UserStatistics(
                userRepository.count(),
                userRepository.countAllByInactiveFalse(),
            )
            Triple(true, stats.total, stats.active)
        } catch (e: Exception) {
            logger.error("Probably, User Service is unavailable.", e)
            Triple(false, null, null)
        }
        val (reminderServiceUp, remindersCountTotal, remindersCountActive) = try {
            val stats = BirthdayStatistics(
                birthdayReminderService.countAll(),
                birthdayReminderService.countNotDeleted(),
            )
            Triple(true, stats.total, stats.active)
        } catch (e: Exception) {
            logger.error("Probably, Birthday Reminder Service is unavailable.", e)
            Triple(false, null, null)
        }
        return Statistics(userServiceUp, usersCountTotal, usersCountActive, reminderServiceUp, remindersCountTotal, remindersCountActive)
    }
}