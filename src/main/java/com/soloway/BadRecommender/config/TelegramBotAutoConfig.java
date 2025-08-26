package com.soloway.BadRecommender.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.soloway.BadRecommender.service.TelegramBotService;

/**
 * Автоматическая конфигурация для Telegram бота
 */
@Configuration
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class TelegramBotAutoConfig {

    @Autowired
    private TelegramBotConfig botConfig;

    @Bean
    public TelegramBotService telegramBotService() {
        System.out.println("✅ Telegram бот автоматически зарегистрирован");
        return new TelegramBotService(botConfig, null, null, null);
    }
}
