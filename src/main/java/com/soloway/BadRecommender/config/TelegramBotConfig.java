package com.soloway.BadRecommender.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.soloway.BadRecommender.service.TelegramBotService;

/**
 * Конфигурация для Telegram бота
 */
@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.username:}")
    private String botUsername;

    @Value("${telegram.bot.enabled:false}")
    private boolean botEnabled;

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public boolean isBotEnabled() {
        return botEnabled;
    }

    public void setBotEnabled(boolean botEnabled) {
        this.botEnabled = botEnabled;
    }

    @Bean
    public TelegramBotService telegramBotService() {
        if (!botEnabled || botToken == null || botToken.isEmpty()) {
            System.out.println("⚠️ TELEGRAM_BOT_TOKEN не установлен - Telegram бот не будет работать");
            return null;
        }
        System.out.println("✅ Telegram бот инициализирован: " + botUsername);
        return new TelegramBotService(this, null, null, null);
    }
}
