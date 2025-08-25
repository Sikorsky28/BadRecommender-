package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Supplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Отправляет email с рекомендациями пользователю
     */
    public void sendRecommendationsEmail(String userEmail, String userName, String selectedTopic, 
                                       List<Supplement> mainRecommendations, 
                                       List<Supplement> additionalRecommendations) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setFrom("marketing@soloways.ru");
            message.setSubject("Ваши персональные рекомендации БАДов от SOLOWAYS");
            
            String emailContent = buildEmailContent(userName, selectedTopic, mainRecommendations, additionalRecommendations);
            message.setText(emailContent);
            
            mailSender.send(message);
            
            System.out.println("✅ Email с рекомендациями отправлен на: " + userEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки email: " + e.getMessage());
            System.err.println("Детали ошибки: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    /**
     * Строит содержимое email
     */
    private String buildEmailContent(String userName, String selectedTopic, 
                                   List<Supplement> mainRecommendations, 
                                   List<Supplement> additionalRecommendations) {
        
        StringBuilder content = new StringBuilder();
        
        content.append(userName).append(", спасибо, что прошли опрос по теме \"").append(getTopicDisplayName(selectedTopic)).append("\".\n\n");
        content.append("На основе ваших данных мы собрали персональные рекомендации по БАДам:\n\n");
        
        // Основные рекомендации
        content.append("ОСНОВНЫЕ РЕКОМЕНДАЦИИ:\n");
        content.append("========================\n");
        
        for (Supplement supplement : mainRecommendations) {
            content.append("• ").append(supplement.getName()).append("\n");
            content.append("  ").append(supplement.getDescription()).append("\n\n");
        }
        
        // Дополнительные рекомендации
        if (!additionalRecommendations.isEmpty()) {
            content.append("ДОПОЛНИТЕЛЬНЫЕ РЕКОМЕНДАЦИИ:\n");
            content.append("==============================\n");
            
            for (Supplement supplement : additionalRecommendations) {
                content.append("• ").append(supplement.getName()).append("\n");
                content.append("  ").append(supplement.getDescription()).append("\n\n");
            }
        }
        
        content.append("Для заказа переходите на наш сайт: https://soloways.ru\n\n");
        content.append("С уважением,\n");
        content.append("Команда SOLOWAYS\n");
        content.append("https://soloways.ru");
        
        return content.toString();
    }

    /**
     * Получает отображаемое название темы
     */
    private String getTopicDisplayName(String topic) {
        switch (topic) {
            case "iron": return "Поднять гемоглобин";
            case "energy": return "Бодрость и энергия";
            case "sleep": return "Крепкий сон, меньше стресса";
            case "weight": return "Контроль веса и аппетита";
            case "skin": return "Чистая кожа, крепкие волосы";
            case "digestion": return "Комфорт пищеварения";
            case "joints": return "Подвижные суставы, крепкие кости";
            case "immunity": return "Сильный иммунитет";
            case "heart": return "Здоровое сердце и сосуды";
            case "thyroid": return "Поддержка щитовидной железы";
            case "female": return "Регулярный цикл, мягкий ПМС";
            case "menopause": return "Менопауза без приливов";
            case "male": return "Мужское здоровье";
            default: return topic;
        }
    }
}
