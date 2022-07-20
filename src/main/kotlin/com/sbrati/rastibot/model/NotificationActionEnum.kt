package com.sbrati.rastibot.model

enum class NotificationActionEnum(
    val supportedDaysBefore: Int,
    val abbreviation: String,
) {
    DO_NOT_NOTIFY_ANYMORE(-1, "dnna"),
    DO_NOT_NOTIFY_THIS_YEAR(0, "dnnty"),
    NOTIFY_WEEK_BEFORE(7, "nwb"),
    NOTIFY_THREE_DAYS_BEFORE(3, "ntdb"),
    NOTIFY_A_DAY_BEFORE(1, "nadb"),
    NOTIFY_AT_THE_DAY(0, "natd");

    companion object {
        fun from(value: String?): NotificationActionEnum? {
            return NotificationActionEnum
                .values()
                .firstOrNull { it.abbreviation == value }
        }
    }
}