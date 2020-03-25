package com.sbrati.rastibot.utils

import com.sbrati.rastibot.model.Person
import me.ivmg.telegram.entities.Contact

fun Contact.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Person.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}