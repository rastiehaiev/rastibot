package com.sbrati.rastibot.model

class NotificationActionResult {

    lateinit var callbackQueryId: String
    lateinit var actionPerformed: String
    lateinit var person: Person

    override fun toString(): String {
        return "NotificationActionResult(callbackQueryId='$callbackQueryId', actionPerformed='$actionPerformed', person=$person)"
    }
}