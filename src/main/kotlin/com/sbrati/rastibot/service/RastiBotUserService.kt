package com.sbrati.rastibot.service

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Update
import com.sbrati.rastibot.entity.UserEntity
import com.sbrati.rastibot.model.User
import com.sbrati.rastibot.repository.UserRepository
import com.sbrati.spring.boot.starter.kotlin.telegram.component.BlockedChatHandler
import com.sbrati.spring.boot.starter.kotlin.telegram.service.AwarenessService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.LocaleService
import com.sbrati.spring.boot.starter.kotlin.telegram.service.UserService
import com.sbrati.spring.boot.starter.kotlin.telegram.util.LoggerDelegate
import com.sbrati.spring.boot.starter.kotlin.telegram.util.chatId
import com.sbrati.spring.boot.starter.kotlin.telegram.util.orElse
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

@Service
class RastiBotUserService(
    private val userRepository: UserRepository,
    private val spaceNotificationService: SpaceNotificationService,
) : UserService<User>, AwarenessService, BlockedChatHandler, LocaleService {

    private val logger by LoggerDelegate()

    private val cache: MutableMap<Long, User> = ConcurrentHashMap()

    override fun getAllChatIds(): List<Long> {
        return userRepository.findAll().map { it.chatId }
    }

    override fun findByChatId(chatId: Long): User? {
        val user: User? = cache[chatId].orElse { getUser(chatId) }
        user?.let { cache[chatId] = it }
        return user
    }

    private fun getUser(chatId: Long): User? {
        return try {
            userRepository.findByChatId(chatId)?.toUser()
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

            var newUser = false
            val chatId = user.chatId ?: return
            var entity = userRepository.findByChatId(chatId)
            if (entity == null) {
                entity = UserEntity()
                entity.chatId = chatId
                newUser = true
            }
            entity.locale = user.locale
            entity.username = user.username
            entity.firstName = user.firstName
            entity.lastName = user.lastName
            entity.inactive = user.inactive
            userRepository.save(entity)

            if (newUser) {
                spaceNotificationService.onNewUser(user)
            }

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
            val uninformedUserIds = userRepository.findByInactiveFalseAndAwarenessNullOrAwarenessLessThan(informLevel).map { it.chatId }
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
        userRepository.setAwareness(chatId, informLevel)
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

    private fun com.github.kotlintelegrambot.entities.User.applyTo(user: User) {
        this.username?.let { user.username = it }
        this.firstName.let { user.firstName = it }
        this.lastName?.let { user.lastName = it }
    }

    private fun UserEntity.toUser(): User {
        return User(
            chatId = this.chatId,
            username = this.username,
            firstName = this.firstName,
            lastName = this.lastName,
            locale = this.locale,
            inactive = this.inactive,
        )
    }
}