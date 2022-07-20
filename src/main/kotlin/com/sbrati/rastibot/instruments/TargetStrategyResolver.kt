package com.sbrati.rastibot.instruments

import com.sbrati.rastibot.model.BirthDayReminderStrategy
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Component
class TargetStrategyResolver(private val clock: Clock) {

    fun resolve(nextBirthdayTimestamp: Long): BirthDayReminderStrategy? {
        val instantAtStartOfDay = clock.instant().truncatedTo(ChronoUnit.DAYS)
        val days = TimeUnit.MILLISECONDS.toDays(nextBirthdayTimestamp - instantAtStartOfDay.toEpochMilli()).toInt()
        return BirthDayReminderStrategy.of(days)
    }
}