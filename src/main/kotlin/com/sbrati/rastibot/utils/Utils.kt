package com.sbrati.rastibot.utils

import com.sbrati.rastibot.model.Person
import me.ivmg.telegram.entities.Contact
import me.ivmg.telegram.entities.Update

fun Contact.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Person.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Update.chatDetails(): String {
    val from = this.message?.from ?: return "<unknown>"
    return from.firstName + (from.lastName?.prependIndent(" ") ?: "") + "(id=${from.id})"
}