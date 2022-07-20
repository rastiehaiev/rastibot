package com.sbrati.rastibot.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "user_table")
open class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = 0

    @Column(name = "chat_id")
    open var chatId: Long = 0

    @Column(name = "username")
    open var username: String? = null

    @Column(name = "first_name")
    open var firstName: String? = null

    @Column(name = "last_name")
    open var lastName: String? = null

    @Column(name = "locale")
    open var locale: String? = null

    @Column(name = "inactive")
    open var inactive = false

    @Column(name = "awareness")
    open  val awareness: Int? = null
}