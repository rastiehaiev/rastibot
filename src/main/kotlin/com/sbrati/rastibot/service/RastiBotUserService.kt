package com.sbrati.rastibot.service

import com.sbrati.rastibot.client.UserServiceClient
import com.sbrati.rastibot.model.User
import com.sbrati.spring.boot.starter.kotlin.telegram.component.BlockedChatHandler
import com.sbrati.spring.boot.starter.kotlin.telegram.service.AwarenessService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.LocaleService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.UserService
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import com.sbrati.spring.boot.starter.kotlin.telegram.util.chatId
import com.sbrati.spring.boot.starter.kotlin.telegram.util.orElse
import me.ivmg.telegram.entities.Chat
import me.ivmg.telegram.entities.Update
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class RastiBotUserService(private val userServiceClient: UserServiceClient)
    : UserService<User>, AwarenessService, BlockedChatHandler, LocaleService {

    private val logger by LoggerDelegate()

    private val cache: MutableMap<Long, User> = ConcurrentHashMap()

    override fun getAllChatIds(): List<Long> = userServiceClient.getAllChatIds()

    override fun findByChatId(chatId: Long): User? {
        val user: User? = cache[chatId].orElse { getUser(chatId) }
        user?.let { cache[chatId] = it }
        return user
    }

    private fun getUser(chatId: Long): User? {
        return try {
            userServiceClient.findByChatId(chatId)
        } catch (e: Exception) {
            logger.error("Failed to find user by chatID=${chatId}. Reason: ${e.message}.")
            null
        }
    }

    override fun apply(update: Update) {
        val chatId = update.chatId() ?: return
        val existingUser = findByChatId(chatId)

        val user = User()
        user.chatId = chatId
        user.locale = existingUser?.locale
        user.inactive = false
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

    override fun onChatIdBlocked(chatId: Long) {
        logger.info("Marking user with charID=$chatId as inactive.")
        val user = findByChatId(chatId)
        user?.let {
            user.inactive = true
            saveOrUpdate(user)
        }
    }

    override fun findUninformedUserIds(informLevel: Int): List<Long> {
        return try {
            val uninformedUserIds = userServiceClient.findUninformedUserIds(informLevel)
            if (uninformedUserIds.isEmpty()) {
                logger.info("No users found to inform.")
            }
            uninformedUserIds
        } catch (e: Exception) {
            logger.error("Failed to get uninformed users. Reason: ${e.message}.")
            emptyList()
        }
    }

    override fun setUserInformLevel(chatId: Long, informLevel: Int) {
        userServiceClient.setUserInformLevel(chatId, informLevel)
    }

    override fun findLocaleByChatId(chatId: Long): Locale? {
        return findByChatId(chatId)?.locale?.let { Locale(it) }
    }

    override fun saveLocale(chatId: Long, locale: Locale) {
        val user = findByChatId(chatId)!!
        if (user.locale != locale.language) {
            user.locale = locale.language
            saveOrUpdate(user)
        }
    }

    private fun Chat.applyTo(user: User) {
        this.username?.let { user.username = it }
        this.firstName?.let { user.firstName = it }
        this.lastName?.let { user.lastName = it }
    }

    private fun me.ivmg.telegram.entities.User.applyTo(user: User) {
        this.username?.let { user.username = it }
        this.firstName.let { user.firstName = it }
        this.lastName?.let { user.lastName = it }
    }
}