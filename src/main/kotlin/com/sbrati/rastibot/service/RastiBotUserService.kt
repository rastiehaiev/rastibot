package com.sbrati.rastibot.service

import com.sbrati.rastibot.client.UserServiceClient
import com.sbrati.rastibot.model.User
import com.sbrati.spring.boot.starter.kotlin.telegram.service.AwarenessService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.UserService
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import com.sbrati.spring.boot.starter.kotlin.telegram.util.chatId
import com.sbrati.spring.boot.starter.kotlin.telegram.util.orElse
import me.ivmg.telegram.entities.Chat
import me.ivmg.telegram.entities.Update
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RastiBotUserService(private val userServiceClient: UserServiceClient) : UserService<User>, AwarenessService {

    private val logger by LoggerDelegate()

    private val cache: MutableMap<Long, User> = ConcurrentHashMap()

    override fun findByChatId(chatId: Long): User? {
        val user: User? = cache[chatId].orElse(try {
            userServiceClient.findByChatId(chatId)
        } catch (e: Exception) {
            logger.error("Failed to find user by chatID=${chatId}. Reason: ${e.message}.")
            null
        })
        user?.let { cache[chatId] = it }
        return user
    }

    override fun apply(update: Update) {
        val chatId = update.chatId() ?: return
        val existingUser = findByChatId(chatId)

        val user = User()
        user.chatId = chatId
        user.locale = existingUser?.locale
        update.message?.chat?.applyTo(user)
        update.callbackQuery?.from?.applyTo(user)

        if (user != existingUser) {
            saveOrUpdate(user)
        }
    }

    override fun saveOrUpdate(user: User) {
        try {
            logger.debug("Creating user {}.", user)
            userServiceClient.createOrUpdate(user.chatId!!, user)
            cache[user.chatId!!] = user
        } catch (e: Exception) {
            logger.error("Failed to create/update user. Reason: ${e.message}.")
        }
    }

    override fun findUninformedUserIds(informLevel: Int): List<Long> {
        val uninformedUserIds = userServiceClient.findUninformedUserIds(informLevel)
        if (uninformedUserIds.isEmpty()) {
            logger.info("No users found to inform.")
        }
        return uninformedUserIds
    }

    override fun setUserInformLevel(chatId: Long, informLevel: Int) {
        userServiceClient.setUserInformLevel(chatId, informLevel)
    }

    private fun Chat.applyTo(user: User) {
        this.username?.let { user.userName = it }
        this.firstName?.let { user.firstName = it }
        this.lastName?.let { user.lastName = it }
    }

    private fun me.ivmg.telegram.entities.User.applyTo(user: User) {
        this.username?.let { user.userName = it }
        this.firstName.let { user.firstName = it }
        this.lastName?.let { user.lastName = it }
    }
}