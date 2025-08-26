package com.soloway.BadRecommender.controller;

import com.soloway.BadRecommender.config.TelegramBotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/telegram")
public class TelegramBotController {

    @Autowired
    private TelegramBotConfig botConfig;

    @GetMapping("/status")
    public String getBotStatus() {
        return String.format("""
            Telegram Bot Status:
            - Enabled: %s
            - Username: %s
            - Token: %s
            """, 
            botConfig.isBotEnabled(),
            botConfig.getBotUsername(),
            botConfig.getBotToken() != null ? "***" : "null"
        );
    }

    @GetMapping("/test")
    public String testBot() {
        if (!botConfig.isBotEnabled()) {
            return "Bot is disabled. Check TELEGRAM_BOT_ENABLED variable.";
        }
        
        if (botConfig.getBotToken() == null || botConfig.getBotToken().isEmpty()) {
            return "Bot token is missing. Check TELEGRAM_BOT_TOKEN variable.";
        }
        
        return "Bot is configured correctly. Try sending /start to @SolowaysRecommendationsBot";
    }
}
