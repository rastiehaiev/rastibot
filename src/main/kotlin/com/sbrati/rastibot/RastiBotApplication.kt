package com.sbrati.rastibot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
open class RastiBotApplication

fun main(args: Array<String>) {
    runApplication<RastiBotApplication>(*args)
}
