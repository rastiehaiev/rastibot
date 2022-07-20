package com.sbrati.rastibot.entity

import com.sbrati.rastibot.model.BirthDayReminderStrategy
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "birthday_reminder")
open class BirthdayReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = 0

    @Column(name = "chat_id")
    open var chatId: Long = 0

    @Column(name = "reminded_user_id")
    open var remindedUserId: String? = null

    @Column(name = "reminded_user_first_name", nullable = false)
    open var remindedUserFirstName: String? = null

    @Column(name = "reminded_user_last_name")
    open var remindedUserLastName: String? = null

    @Column(name = "next_birthday_timestamp")
    open var nextBirthDayTimestamp: Long = 0

    @Column(name = "day")
    open var day = 0

    @Column(name = "month")
    open var month = 0

    @Column(name = "year")
    open var year: Int? = null

    @Column(name = "preferred_strategy")
    @Enumerated(value = EnumType.STRING)
    open var preferredStrategy: BirthDayReminderStrategy? = null

    @Column(name = "last_notified_days")
    open var lastNotifiedDays: Int? = null

    @Column(name = "last_updated")
    open var lastUpdated: Long? = null

    @Column(name = "disabled")
    open var disabled = false

    @Column(name = "deleted")
    open var deleted = false

    override fun toString(): String {
        return "BirthdayReminderEntity(id=$id, chatId=$chatId, remindedUserId=$remindedUserId, remindedUserFirstName=$remindedUserFirstName, remindedUserLastName=$remindedUserLastName, nextBirthDayTimestamp=$nextBirthDayTimestamp, day=$day, month=$month, year=$year, preferredStrategy=$preferredStrategy, lastNotifiedDays=$lastNotifiedDays, lastUpdated=$lastUpdated, disabled=$disabled, deleted=$deleted)"
    }
}
