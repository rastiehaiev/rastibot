package com.sbrati.rastibot.client

import com.sbrati.rastibot.model.User
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(name = "rastibot-user-service", decode404 = true)
interface UserServiceClient {

    @GetMapping("/users/chat/{id}")
    fun findByChatId(@PathVariable("id") chatId: Long): User?

    @PostMapping("/users/chat/{id}")
    fun create(@PathVariable("id") user: User)
}