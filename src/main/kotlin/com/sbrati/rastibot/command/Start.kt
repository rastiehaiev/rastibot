package com.sbrati.rastibot.command

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpContext
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import org.springframework.stereotype.Component

@Component
open class Start : TelegramCommand<NoOpContext>("start", NoOpContext::class.java, false, false) {
    init {
        apply {
            stage("start") {
                start { _, context ->
                    finish {
                        message {
                            key = "start.info.welcome.message"
                            args = listOf(context.firstName)
                            parseMode = ParseMode.MARKDOWN
                        }
                    }
                }
            }
        }
    }
}
