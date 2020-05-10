package com.sbrati.rastibot.utils

import com.sbrati.rastibot.model.Person
import me.ivmg.telegram.entities.Contact
import me.ivmg.telegram.entities.Update
import me.ivmg.telegram.entities.User

fun Contact.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Contact.asPerson(): Person {
    return Person(chatId = this.userId!!, firstName = this.firstName, lastName = this.lastName)
}

fun User.asPerson(): Person {
    return Person(chatId = this.id, firstName = this.firstName, lastName = this.lastName)
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