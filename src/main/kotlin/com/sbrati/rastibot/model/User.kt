package com.sbrati.rastibot.model

class User(
    var chatId: Long? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var locale: String? = null,
    var inactive: Boolean = false
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (chatId != other.chatId) return false
        if (username != other.username) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (locale != other.locale) return false
        if (inactive != other.inactive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chatId?.hashCode() ?: 0
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (locale?.hashCode() ?: 0)
        result = 31 * result + inactive.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(chatId=$chatId, username=$username, firstName=$firstName, lastName=$lastName, locale=$locale, inactive=$inactive)"
    }
}