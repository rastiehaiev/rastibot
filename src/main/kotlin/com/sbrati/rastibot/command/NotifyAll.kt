package com.sbrati.rastibot.command

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpContext
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.forwardedMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.route
import org.springframework.stereotype.Component

@Component
open class NotifyAll: TelegramCommand<NoOpContext>(name = "notifyall", admin = true, contextType = NoOpContext::class.java) {

    init {
        stage("notifyall") {
            start { _, _ ->
                message { key = "notifyall.info.compose.message.to.all.users" }
            }
            update { update, _ ->
                finish {
                    route {
                        sender {
                            message { key = "notifyall.info.message.has.been.sent" }
                        }
                        everyone {
                            forwardedMessage {
                                parseMode = ParseMode.MARKDOWN
                                original = update
                            }
                        }
                    }
                }
            }
        }
    }
}
