package com.sbrati.rastibot.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "rastibot")
data class RastiBotProperties(val googleCloud: GoogleCloudConfig)

data class GoogleCloudConfig(val projectId: String, val pubSub: GoogleCloudPubSubConfig)

data class GoogleCloudPubSubConfig(val publish: GoogleCloudPublishersConfig, val subscribe: GoogleCloudSubscribersConfig)

data class GoogleCloudPublishersConfig(val topics: GoogleCloudPubSubTopics)

data class GoogleCloudPubSubTopics(val birthDayReminderCreate: GoogleCloudPubSubTopic)

data class GoogleCloudPubSubTopic(val name: String)

data class GoogleCloudSubscribersConfig(val subscriptions: GoogleCloudPubSubSubscriptions)

data class GoogleCloudPubSubSubscriptions(val birthdayReminderCreateSuccess: GoogleCloudPubSubSubscription,
                                          val birthdayReminderCreateConflict: GoogleCloudPubSubSubscription)

data class GoogleCloudPubSubSubscription(val name: String)

fun RastiBotProperties.googleCloudTopics(): GoogleCloudPubSubTopics {
    return this.googleCloud.pubSub.publish.topics
}

fun RastiBotProperties.googleCloudSubscriptions(): GoogleCloudPubSubSubscriptions {
    return this.googleCloud.pubSub.subscribe.subscriptions
}