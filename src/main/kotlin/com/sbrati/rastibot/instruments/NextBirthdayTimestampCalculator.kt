package com.sbrati.rastibot.instruments

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.Month
import java.time.OffsetDateTime
import java.time.Year
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Component
class NextBirthdayTimestampCalculator(private val clock: Clock) {

    fun nextBirthdayTimestamp(monthNumber: Int, day: Int): Long {
        val offsetDateTime: OffsetDateTime = clock.instant().truncatedTo(ChronoUnit.DAYS).atOffset(ZoneOffset.UTC)
        val currentYear = offsetDateTime.year
        val month = Month.of(monthNumber)
        var birthday = getLocalDate(currentYear, month, day)
        if (birthday.isBefore(offsetDateTime.toLocalDate())) {
            birthday = getLocalDate(currentYear + 1, month, day)
        }
        return birthday.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    private fun getLocalDate(currentYear: Int, month: Month, day: Int): LocalDate {
        var normalisedDay = day
        if (month == Month.FEBRUARY && normalisedDay == 29 && !Year.isLeap(currentYear.toLong())) {
            normalisedDay = 28
        }
        return LocalDate.of(currentYear, month.value, normalisedDay)
    }
}
