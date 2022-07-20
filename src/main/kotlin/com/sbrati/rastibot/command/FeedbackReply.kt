package com.sbrati.rastibot.command

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.rastibot.model.ReplyFeedback
import com.sbrati.rastibot.model.callback.getChatIdFromCallbackData
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.forwardedMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.route
import org.springframework.stereotype.Component

@Component
open class FeedbackReply : TelegramCommand<ReplyFeedback>(
    name = "replfbck",
    admin = true,
    synthetic = true,
    contextType = ReplyFeedback::class.java,
) {

    init {
        stage("compose") {
            start { update, context ->
                context.receiverChatId = update?.getChatIdFromCallbackData()
                message {
                    key = "replyfeedback.compose.message.to.user"
                }
            }
            update { update, context ->
                finish {
                    route {
                        val receiverChatId = context.receiverChatId
                        receiverChatId?.let {
                            sender {
                                message {
                                    key = "replyfeedback.message.has.been.sent"
                                    parseMode = ParseMode.MARKDOWN
                                }
                            }
                            receiver(receiverChatId) {
                                forwardedMessage {
                                    original = update
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
