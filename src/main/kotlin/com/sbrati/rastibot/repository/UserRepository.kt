package com.sbrati.rastibot.repository

import com.sbrati.rastibot.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {

    fun findByChatId(chatId: Long): UserEntity?

    fun findByInactiveFalseAndAwarenessNullOrAwarenessLessThan(awareness: Int): List<UserEntity>

    fun countAllByInactiveFalse(): Long

    @Modifying
    @Query(value = "UPDATE user_table SET awareness = :awareness WHERE chat_id = :chatId")
    fun setAwareness(@Param("chatId") chatId: Long, @Param("awareness") awareness: Int)
}
