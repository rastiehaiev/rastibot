package com.sbrati.rastibot.model

class NotificationAction {

    var notificationId: Long = 0
    var callbackQueryId: String? = null
    var action: String? = null

    override fun toString(): String {
        return "NotificationAction(notificationId=$notificationId, callbackQueryId=$callbackQueryId, action=$action)"
    }
}