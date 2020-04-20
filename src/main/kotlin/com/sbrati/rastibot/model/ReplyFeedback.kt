package com.sbrati.rastibot.model

import com.sbrati.spring.boot.starter.kotlin.telegram.command.Context

class ReplyFeedback : Context() {
     var receiverChatId: Long? = null
}