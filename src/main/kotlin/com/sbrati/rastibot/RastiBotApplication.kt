package com.sbrati.rastibot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
open class RastiBotApplication

fun main(args: Array<String>) {
    runApplication<RastiBotApplication>(*args)
}
