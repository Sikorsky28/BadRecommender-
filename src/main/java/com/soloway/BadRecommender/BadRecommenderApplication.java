package com.soloway.BadRecommender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class BadRecommenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadRecommenderApplication.class, args);
	}

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		
		                // Проверяем наличие Telegram webhook контроллера
                try {
                    Object telegramWebhook = context.getBean("telegramWebhookController");
                    System.out.println("✅ Telegram webhook контроллер успешно зарегистрирован в Spring контексте");
                } catch (Exception e) {
                    System.out.println("⚠️ Telegram webhook контроллер не найден в Spring контексте: " + e.getMessage());
                }
		
		// Проверяем переменные окружения
		String botEnabled = System.getenv("TELEGRAM_BOT_ENABLED");
		String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
		String botUsername = System.getenv("TELEGRAM_BOT_USERNAME");
		
		System.out.println("🔍 Проверка переменных окружения Telegram бота:");
		System.out.println("  TELEGRAM_BOT_ENABLED: " + botEnabled);
		System.out.println("  TELEGRAM_BOT_TOKEN: " + (botToken != null ? "***" : "null"));
		System.out.println("  TELEGRAM_BOT_USERNAME: " + botUsername);
	}
}
