package com.sbrati.rastibot

import com.sbrati.rastibot.model.Person
import com.sbrati.spring.starter.telegram.dsl.Transaction
import me.ivmg.telegram.entities.Contact

fun Contact.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Person.fullName(): String {
    return this.firstName + (this.lastName?.prependIndent(" ") ?: "")
}

fun Transaction.setContactFullName(contact: Contact) {
    this.put("contactFullName", contact.fullName())
}

fun Transaction.getContactFullName(): String {
    return this.getString("contactFullName")
}

fun Transaction.setLanguage(language: String) {
    this.put("language", language)
}

fun Transaction.getLanguage(): String {
    return this.getString("language")
}