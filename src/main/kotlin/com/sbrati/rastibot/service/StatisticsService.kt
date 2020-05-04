package com.sbrati.rastibot.service

import com.sbrati.rastibot.client.BirthdayReminderServiceClient
import com.sbrati.rastibot.client.UserServiceClient
import com.sbrati.rastibot.model.Statistics
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import org.springframework.stereotype.Service

@Service
class StatisticsService(private val userServiceClient: UserServiceClient,
                        private val birthdayReminderServiceClient: BirthdayReminderServiceClient) {

    private val logger by LoggerDelegate()

    fun getStatistics(): Statistics {
        val (userServiceUp, usersCount) = try {
            Pair(true, userServiceClient.count())
        } catch (e: Exception) {
            logger.error("Probably, User Service is unavailable.", e)
            Pair(false, null)
        }
        val (reminderServiceUp, remindersCountTotal, remindersCountActive) = try {
            val stats = birthdayReminderServiceClient.stats()
            Triple(true, stats.total, stats.active)
        } catch (e: Exception) {
            logger.error("Probably, Birthday Reminder Service is unavailable.", e)
            Triple(false, null, null)
        }
        return Statistics(userServiceUp, usersCount, reminderServiceUp, remindersCountTotal, remindersCountActive)
    }
}