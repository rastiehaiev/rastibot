package com.sbrati.rastibot.utils

import com.github.kotlintelegrambot.entities.Contact
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import com.sbrati.rastibot.model.Person

fun Contact.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Contact.asPerson(): Person {
    return Person(chatId = this.userId, firstName = this.firstName, lastName = this.lastName)
}

fun User.asPerson(): Person {
    return Person(chatId = this.id, firstName = this.firstName, lastName = this.lastName)
}

fun String.asPerson(): Person {
    val firstNameAndLastName = this.trim().split(" ", limit = 2)
    val (firstName, lastName) = if (firstNameAndLastName.size == 2) {
        firstNameAndLastName[0] to firstNameAndLastName[1]
    } else {
        firstNameAndLastName[0] to null
    }
    return Person(chatId = null, firstName = firstName, lastName = lastName)
}

fun Person.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Update.fullName(): String {
    val from = this.message?.from ?: return "<unknown>"
    return from.firstName + (from.lastName?.prependIndent(" ") ?: "")
}

fun Boolean.status(): String {
    return if (this) {
        "UP"
    } else {
        "DOWN"
    }
}

fun Long?.orUnknown(): String {
    return this?.toString() ?: "?"
}