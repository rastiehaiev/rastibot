package com.sbrati.rastibot.service

import com.sbrati.rastibot.model.Person
import com.sbrati.rastibot.model.User
import com.sbrati.rastibot.utils.fullName
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import space.jetbrains.api.runtime.SpaceHttpClient
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.withServiceAccountTokenSource

@Service
class SpaceNotificationService {

    private val space = SpaceHttpClient(HttpClient(Apache)).withServiceAccountTokenSource(
        clientId = "2a45c275-6212-4bdd-8de1-486b63f7ddfd",
        clientSecret = "6dc35321d16c69f06724fdaf3137f05a8a3efbf7441946cd1ee8e10d14667d5d",
        serverUrl = "https://sbrati.jetbrains.space",
    )

    fun onNewUser(user: User) {
        GlobalScope.launch {
            space.chats.channels.messages.sendTextMessage(
                channel = "xmgnK2IjkP3",
                text = "New user!\nID=${user.chatId}\nUsername: ${user.username}\nFirst Name: ${user.firstName}\nLast Name: ${user.lastName}."
            )
        }
    }

    fun onNewBirthdayReminder(chatId: Long, date: String, person: Person) {
        GlobalScope.launch {
            space.chats.channels.messages.sendTextMessage(
                channel = "xmgnK2IjkP3",
                text = "User with ID=$chatId created birthday reminder:\n${person.fullName()}\n$date."
            )
        }
    }
}
