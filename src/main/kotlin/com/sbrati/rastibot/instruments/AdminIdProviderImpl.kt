package com.sbrati.rastibot.instruments

import com.sbrati.rastibot.properties.RastiBotProperties
import com.sbrati.spring.boot.starter.kotlin.telegram.component.AdminChatIdsProvider
import org.springframework.stereotype.Component

@Component
class AdminIdProviderImpl(private val properties: RastiBotProperties) : AdminChatIdsProvider {

    override fun adminChatIds(): List<Long> = listOf(properties.adminChatId)
}