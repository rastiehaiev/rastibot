package com.sbrati.rastibot.configuration

import com.sbrati.rastibot.fullName
import com.sbrati.rastibot.model.CreateBirthDayReminderConflict
import com.sbrati.rastibot.model.CreateBirthDayReminderSuccess
import com.sbrati.rastibot.properties.RastiBotProperties
import com.sbrati.rastibot.properties.googleCloudSubscriptions
import com.sbrati.spring.starter.telegram.model.SmartBot
import com.sbrati.spring.starter.telegram.model.key
import com.sbrati.spring.starter.telegram.utils.LoggerDelegate
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
open class RastiBotPubSubConfiguration(private val smartBot: SmartBot,
                                       private val template: PubSubTemplate,
                                       private val properties: RastiBotProperties) {

    private val logger by LoggerDelegate()

    @EventListener(ApplicationReadyEvent::class)
    fun subscribe() {
        val birthdayReminderCreateSuccess = properties.googleCloudSubscriptions().birthdayReminderCreateSuccess
        template.subscribe(birthdayReminderCreateSuccess.name) { ackMessage ->
            try {
                val birthDayReminderSuccess = getBirthDayReminderSuccess(ackMessage)
                val message = key("success.birthdayreminder.created").arg(birthDayReminderSuccess.birthDayReminder.person.fullName())
                smartBot.sendMessage(chatId = birthDayReminderSuccess.birthDayReminder.chatId, message = message)
            } catch (e: Exception) {
                logger.error("Failed to send success message. Reason: ${e.message}", e)
            } finally {
                ackMessage.ack()
            }
        }
        val birthdayReminderCreateConflict = properties.googleCloudSubscriptions().birthdayReminderCreateConflict
        template.subscribe(birthdayReminderCreateConflict.name) { ackMessage ->
            try {
                val birthDayReminderConflict = getBirthDayReminderConflict(ackMessage)
                logger.info("Received conflict: $birthDayReminderConflict.")
            } catch (e: Exception) {
                logger.error("Failed to process conflict message. Reason: ${e.message}", e)
            } finally {
                ackMessage.ack()
            }
        }
    }

    private fun getBirthDayReminderConflict(message: BasicAcknowledgeablePubsubMessage) =
            template.messageConverter.fromPubSubMessage(message.pubsubMessage, CreateBirthDayReminderConflict::class.java)

    private fun getBirthDayReminderSuccess(message: BasicAcknowledgeablePubsubMessage) =
            template.messageConverter.fromPubSubMessage(message.pubsubMessage, CreateBirthDayReminderSuccess::class.java)
}