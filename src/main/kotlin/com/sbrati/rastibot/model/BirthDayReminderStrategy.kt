package com.sbrati.rastibot.model

enum class BirthDayReminderStrategy(val daysAmount: Int, val abbreviation: String) {

    TWO_WEEKS_BEFORE(14, "twb"),
    A_WEEK_BEFORE(7, "awb"),
    THREE_DAYS_BEFORE(3, "tdb"),
    A_DAY_BEFORE(1, "adb"),
    ON_A_DAY(0, "oad");

    companion object {

        val MAX_DAYS_AMOUNT: Int = getMaxDaysAmount()

        fun of(days: Int): BirthDayReminderStrategy? {
            for (value in values()) {
                if (value.daysAmount == days) {
                    return value
                }
            }
            return null
        }

        private fun getMaxDaysAmount(): Int {
            return values()
                .map { it.daysAmount }
                .maxByOrNull { it }
                ?: error("Failed to get max value of days amount.")
        }
    }
}