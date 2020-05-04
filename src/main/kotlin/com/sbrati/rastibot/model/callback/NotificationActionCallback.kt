package com.sbrati.rastibot.model.callback

import com.sbrati.spring.boot.starter.kotlin.telegram.model.callback.CallbackDataObject

class NotificationActionCallback : CallbackDataObject() {

    var notificationId: Long = 0
    lateinit var action: String

    override fun getKey(): String = "notificationaction"

    override fun asString(): String {
        return "${getKey()}|n:$notificationId;a:$action"
    }

    override fun construct(value: String) {
        val keyAndValue = value.split("|")
        if (keyAndValue.size != 2 || keyAndValue[0] != getKey()) {
            failToInstantiateNotificationActionCallback(value)
        }

        val cleanValue = keyAndValue[1]
        val notificationAndActionEntries = cleanValue.split(";")
        if (notificationAndActionEntries.size != 2) {
            failToInstantiateNotificationActionCallback(cleanValue)
        }
        val notificationEntry = notificationAndActionEntries[0]
        val notificationKeyAndValue = notificationEntry.split(":")
        if (notificationKeyAndValue.size != 2) {
            failToInstantiateNotificationActionCallback(cleanValue)
        }
        this.notificationId = notificationKeyAndValue[1].toLong()

        val actionEntry = notificationAndActionEntries[1]
        val actionKeyAndValue = actionEntry.split(":")
        if (actionKeyAndValue.size != 2) {
            failToInstantiateNotificationActionCallback(cleanValue)
        }
        this.action = actionKeyAndValue[1]
    }

    private fun failToInstantiateNotificationActionCallback(value: String) {
        throw IllegalStateException("Failed to instantiate NotificationActionCallback from string $value")
    }
}

fun notificationAction(id: Long, action: String): NotificationActionCallback {
    val notificationActionCallback = NotificationActionCallback()
    notificationActionCallback.notificationId = id
    notificationActionCallback.action = action
    return notificationActionCallback
}