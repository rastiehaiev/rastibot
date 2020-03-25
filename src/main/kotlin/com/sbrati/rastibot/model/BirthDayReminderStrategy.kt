package com.sbrati.rastibot.model

enum class BirthDayReminderStrategy(val abbreviation: String) {

    TWO_WEEKS_BEFORE("twb"),
    A_WEEK_BEFORE("awb"),
    THREE_DAYS_BEFORE("tdb"),
    A_DAY_BEFORE("adb"),
    ON_A_DAY("oad")
}