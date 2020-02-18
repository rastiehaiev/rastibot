package com.sbrati.rastibot.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.sbrati.rastibot.model.BirthDayReminder
import com.sbrati.rastibot.properties.RastiBotProperties
import com.sbrati.rastibot.properties.googleCloudTopics
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.stereotype.Component


@Component
class BirthDayReminderPublisher(private val objectMapper: ObjectMapper,
                                private val template: PubSubTemplate,
                                private val properties: RastiBotProperties) {

    fun createReminder(reminder: BirthDayReminder) {
        val data: ByteString = ByteString.copyFrom(objectMapper.writeValueAsBytes(reminder))
        val pubsubMessage = PubsubMessage.newBuilder().setData(data).build()
        template.publish(properties.googleCloudTopics().birthDayReminderCreate.name, pubsubMessage)
    }
}