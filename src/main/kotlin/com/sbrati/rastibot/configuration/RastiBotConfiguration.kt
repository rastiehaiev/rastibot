package com.sbrati.rastibot.configuration

import com.sbrati.rastibot.getContactFullName
import com.sbrati.rastibot.getLanguage
import com.sbrati.rastibot.service.BirthDayReminderService
import com.sbrati.rastibot.service.LanguagePreferencesService
import com.sbrati.spring.starter.telegram.dsl.*
import com.sbrati.spring.starter.telegram.model.key
import com.sbrati.spring.starter.telegram.view.keyboard
import com.sbrati.spring.starter.telegram.view.row
import com.sbrati.spring.starter.telegram.view.supportedLanguagesButtons
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
open class RastiBotConfiguration(private val languagePreferencesService: LanguagePreferencesService,
                                 private val birthDayReminderService: BirthDayReminderService) {

    @Bean
    open fun telegramOperations(): TelegramOperations {
        return telegram {
            languages {
                defaults("English" to Locale("en"))
                        .then("Українська" to Locale("uk"))
                        .then("Русский" to Locale("ru"))
            }
            transaction("setlanguage") {
                stage {
                    name = "CHOOSE_LANGUAGE"
                    message { key("info.language.choose.from.list") }
                    view = supportedLanguagesButtons()
                    expect { update, transaction ->
                        languagePreferencesService.updateLanguagePreferences(update, transaction)
                    }
                }
                finish { transaction ->
                    key("success.language.preferences.changed").arg(transaction.getLanguage())
                }
            }
            transaction("birthdayreminder") {
                stage {
                    name = "REQUEST_CONTACT"
                    message { transaction ->
                        key("info.birthdayreminder.please.send.contact").arg(transaction.userName!!)
                    }
                    expect { update, transaction ->
                        birthDayReminderService.processContact(update, transaction)
                    }
                }
                stage {
                    name = "REQUEST_MONTH"
                    message { key("info.birthdayreminder.please.send.month") }
                    view = keyboard {
                        row(key("month.january"), key("month.february"), key("month.march"))
                        row(key("month.april"), key("month.may"), key("month.june"))
                        row(key("month.july"), key("month.august"), key("month.september"))
                        row(key("month.october"), key("month.november"), key("month.december"))
                    }
                    expect { update, transaction ->
                        birthDayReminderService.processMonth(update, transaction)
                    }
                }
                stage {
                    name = "REQUEST_DAY"
                    message { transaction ->
                        key("info.birthdayreminder.please.send.day").arg(transaction.getContactFullName())
                    }
                    expect { update, transaction ->
                        birthDayReminderService.processDay(update, transaction)
                    }
                }
            }
        }
    }
}