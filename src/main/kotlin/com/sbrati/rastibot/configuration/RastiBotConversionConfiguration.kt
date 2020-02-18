package com.sbrati.rastibot.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RastiBotConversionConfiguration(private val objectMapper: ObjectMapper) {

    @Bean
    open fun pubSubMessageConverter(): PubSubMessageConverter? {
        return JacksonPubSubMessageConverter(objectMapper)
    }
}