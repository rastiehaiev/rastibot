package com.sbrati.rastibot.command

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.rastibot.model.callback.chatIdCallback
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpContext
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.forwardedMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.InlineKeyboardButton
import com.sbrati.spring.boot.starter.kotlin.telegram.model.replyview.inlineKeyboard
import com.sbrati.spring.boot.starter.kotlin.telegram.model.route
import com.sbrati.spring.boot.starter.kotlin.telegram.util.chatId
import com.sbrati.spring.boot.starter.kotlin.telegram.util.fullName
import org.springframework.stereotype.Component

@Component
open class Feedback: TelegramCommand<NoOpContext>("feedback", NoOpContext::class.java) {

    init {
        stage("feedback") {
            start { _, _ ->
                message { key = "feedback.info.specify.your.feedback" }
            }
            update { update, _ ->
                finish {
                    route {
                        sender {
                            message { key = "feedback.info.thanks.for.your.feedback" }
                        }
                        admin {
                            val chatId = update.chatId()
                            forwardedMessage {
                                original = update
                                textWrapperKey = "feedback.info.admin.message"
                                args = listOf(update.fullName(), chatId.toString())
                                parseMode = ParseMode.MARKDOWN
                                replyView = chatId?.let {
                                    inlineKeyboard {
                                        row(
                                            InlineKeyboardButton(
                                            global = true,
                                            key = "inline.button.reply.feedback",
                                            callbackData = chatIdCallback("replfbck", chatId)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
