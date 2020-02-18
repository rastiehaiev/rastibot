package com.sbrati.rastibot.component

import org.springframework.stereotype.Component
import java.time.Month

// TODO make it more flexible when the new language is available
@Component
class MonthResolver {

    fun resolve(value: String): Month? {
        return when (value.toLowerCase()) {
            in arrayOf("january", "январь", "січень") -> Month.JANUARY
            in arrayOf("february", "февраль", "лютий") -> Month.FEBRUARY
            in arrayOf("march", "март", "березень") -> Month.MARCH
            in arrayOf("april", "апрель", "квітень") -> Month.APRIL
            in arrayOf("may", "май", "травень") -> Month.MAY
            in arrayOf("june", "июнь", "червень") -> Month.JUNE
            in arrayOf("july", "июль", "липень") -> Month.JULY
            in arrayOf("august", "август", "серпень") -> Month.AUGUST
            in arrayOf("september", "сентябрь", "вересень") -> Month.SEPTEMBER
            in arrayOf("october", "октябрь", "жовтень") -> Month.OCTOBER
            in arrayOf("november", "ноябрь", "листопад") -> Month.NOVEMBER
            in arrayOf("december", "декабрь", "грудень") -> Month.DECEMBER
            else -> null
        }
    }
}