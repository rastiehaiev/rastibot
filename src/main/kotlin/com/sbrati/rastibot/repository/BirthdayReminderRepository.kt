package com.sbrati.rastibot.repository

import com.sbrati.rastibot.entity.BirthdayReminderEntity
import org.springframework.data.domain.Pageable

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BirthdayReminderRepository : CrudRepository<BirthdayReminderEntity, Long> {

    fun findByChatIdAndRemindedUserId(chatId: Long, remindedUserId: String): BirthdayReminderEntity?

    fun findByChatIdAndRemindedUserIdAndDeletedFalse(chatId: Long, remindedUserId: String): BirthdayReminderEntity?

    fun countAllByDeletedFalse(): Long

    @Query(
        value = "SELECT * FROM birthday_reminder WHERE next_birthday_timestamp < :upcomingTimestamp AND deleted = false " +
                "AND (last_updated is NULL OR last_updated < :lastUpdatedTimestamp)", nativeQuery = true
    )
    fun findUpcoming(
        @Param("upcomingTimestamp") upcomingTimestamp: Long,
        @Param("lastUpdatedTimestamp") lastUpdatedGap: Long,
        pageable: Pageable?
    ): List<BirthdayReminderEntity>

    @Modifying
    @Query(value = "UPDATE birthday_reminder SET last_notified_days = :last_notified_days WHERE id = :id")
    fun updateLastNotifiedDays(@Param("id") id: Long, @Param("last_notified_days") lastNotifiedDays: Int?)

    @Query(
        value = "SELECT * FROM birthday_reminder WHERE chat_id = :chatId AND deleted = false " +
                "ORDER BY next_birthday_timestamp LIMIT 1", nativeQuery = true
    )
    fun findNearest(@Param("chatId") chatId: Long): BirthdayReminderEntity?

    @Query(
        value = "SELECT * FROM birthday_reminder WHERE chat_id = :chatId AND deleted = false " +
                "ORDER BY next_birthday_timestamp LIMIT 3", nativeQuery = true
    )
    fun findThreeNearest(@Param("chatId") chatId: Long): List<BirthdayReminderEntity>

    fun findAllByChatIdAndDeletedFalseOrderByNextBirthDayTimestamp(chatId: Long, pageable: Pageable?): List<BirthdayReminderEntity>

    @Query(
        value = "SELECT * FROM birthday_reminder WHERE chat_id = :chatId AND month = :month AND deleted = false " +
                "ORDER BY next_birthday_timestamp LIMIT 10", nativeQuery = true
    )
    fun findByMonth(@Param("chatId") chatId: Long, @Param("month") month: Int): List<BirthdayReminderEntity>
}
