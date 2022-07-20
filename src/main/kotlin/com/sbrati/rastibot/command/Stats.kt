package com.sbrati.rastibot.command

import com.github.kotlintelegrambot.entities.ParseMode
import com.sbrati.rastibot.service.StatisticsService
import com.sbrati.rastibot.utils.orUnknown
import com.sbrati.rastibot.utils.status
import com.sbrati.spring.boot.starter.kotlin.telegram.command.TelegramCommand
import com.sbrati.spring.boot.starter.kotlin.telegram.command.impl.NoOpContext
import com.sbrati.spring.boot.starter.kotlin.telegram.model.finish
import com.sbrati.spring.boot.starter.kotlin.telegram.model.message.message
import org.springframework.context.annotation.Configuration

@Configuration
open class Stats(
    private val statisticsService: StatisticsService,
) : TelegramCommand<NoOpContext>(
    name = "stats",
    admin = true,
    contextType = NoOpContext::class.java,
) {

    init {
        stage("stats") {
            start { _, _ ->
                val statistics = statisticsService.getStatistics()
                finish {
                    message {
                        key = "stats.info.display.statistics.information"
                        args = listOf(
                            statistics.userServiceUp.status(),
                            statistics.usersCountTotal.orUnknown(),
                            statistics.usersCountActive.orUnknown(),
                            statistics.reminderServiceUp.status(),
                            statistics.remindersCountTotal.orUnknown(),
                            statistics.remindersCountActive.orUnknown()
                        )
                        parseMode = ParseMode.MARKDOWN
                    }
                }
            }
        }
    }
}
