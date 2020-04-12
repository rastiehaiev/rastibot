package com.sbrati.rastibot.client

import com.sbrati.rastibot.model.User
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(url = "\${rastibot.user-service.url}", name = "rastibot-user-service", decode404 = true)
interface UserServiceClient {

    @GetMapping("/users/chat/{id}")
    fun findByChatId(@PathVariable("id") chatId: Long): User?

    @PutMapping("/users/chat/{id}")
    fun createOrUpdate(@PathVariable("id") chatId: Long, @RequestBody user: User)

    @GetMapping("/users/count")
    fun count(): Long

    @GetMapping("/users/uninformed")
    fun findUninformedUserIds(@RequestParam("awareness") awareness: Int): List<Long>

    @PutMapping("/users/chat/{id}/awareness")
    fun setUserInformLevel(@PathVariable("id") chatId: Long, @RequestParam("awareness") awareness: Int)

    @GetMapping("/users/chat_ids")
    fun getAllChatIds(): List<Long>
}