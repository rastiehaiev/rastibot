package com.sbrati.rastibot.configuration

import com.sbrati.rastibot.model.ReplyFeedback
import com.sbrati.rastibot.model.callback.getChatIdFromCallbackData
import com.sbrati.rastibot.service.StatisticsService
import com.sbrati.rastibot.utils.orUnknown
import com.sbrati.rastibot.utils.status
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.forwardedMessage
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import com.sbrati.spring.boot.starter.kotlin.telegram.model.route
import me.ivmg.telegram.entities.ParseMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RastiBotSystemOperationsConfiguration {

    @Bean
    open fun commandNotifyAll(): TelegramCommand<NoOpCommand> {
        return notifyAll {
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

    @Bean
    open fun commandStats(statisticsService: StatisticsService): TelegramCommand<NoOpCommand> {
        return stats {
            stage("stats") {
                start { _, _ ->
                    val statistics = statisticsService.getStatistics()
                    finish {
                        message {
                            key = "stats.info.display.statistics.information"
                            args = listOf(
                                    statistics.userServiceUp.status(),
                                    statistics.usersCount.orUnknown(),
                                    statistics.reminderServiceUp.status(),
                                    statistics.remindersCountTotal.orUnknown(),
                                    statistics.remindersCountActive.orUnknown())
                            parseMode = ParseMode.MARKDOWN
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun commandReplyFeedback(): TelegramCommand<ReplyFeedback> {
        return replyFeedback {
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

    private fun notifyAll(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("notifyall", admin = true) {
            override fun createContext(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun stats(operations: TelegramCommand<NoOpCommand>.() -> Unit): TelegramCommand<NoOpCommand> {
        return object : TelegramCommand<NoOpCommand>("stats", admin = true) {
            override fun createContext(): NoOpCommand {
                return NoOpCommand()
            }
        }.apply(operations)
    }

    private fun replyFeedback(operations: TelegramCommand<ReplyFeedback>.() -> Unit): TelegramCommand<ReplyFeedback> {
        return object : TelegramCommand<ReplyFeedback>("replfbck", admin = true, synthetic = true) {
            override fun createContext(): ReplyFeedback {
                return ReplyFeedback()
            }
        }.apply(operations)
    }
}