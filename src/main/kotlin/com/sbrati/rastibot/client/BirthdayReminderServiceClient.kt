package com.sbrati.rastibot.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(url = "\${rastibot.birthday-reminder-service.url}", name = "rastibot-birthday-reminder-service", decode404 = true)
interface BirthdayReminderServiceClient {

    @GetMapping("/reminders/count")
    fun count(): Long
}