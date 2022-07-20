package com.sbrati.rastibot.model

import java.time.Month

class Birthday(val day: Int, val month: Month, val year: Int?) {

    override fun toString(): String {
        return "Birthday(day=$day, month=$month, year=$year)"
    }
}