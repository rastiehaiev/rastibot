package com.sbrati.rastibot.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "rastibot")
class RastiBotProperties(val adminChatId: Long)
