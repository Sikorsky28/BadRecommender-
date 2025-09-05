package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Supplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Отправляет простой email
     */
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("marketing@soloways.ru");
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            
            System.out.println("✅ Email отправлен на: " + to);
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки email: " + e.getMessage());
            System.err.println("Детали ошибки: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

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
     * Отправляет красивый HTML email с рекомендациями пользователю
     */
    public void sendHtmlRecommendationsEmail(String userEmail, String userName, String selectedTopic, 
                                           List<Supplement> mainRecommendations, 
                                           List<Supplement> additionalRecommendations) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            
            helper.setTo(userEmail);
            helper.setFrom("marketing@soloways.ru");
            helper.setSubject("Ваши персональные рекомендации БАДов от SOLOWAYS");
            
            String htmlContent = buildHtmlEmailContent(userName, selectedTopic, mainRecommendations, additionalRecommendations);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
            System.out.println("✅ HTML Email с рекомендациями отправлен на: " + userEmail);
            
        } catch (MessagingException e) {
            System.err.println("❌ Ошибка отправки HTML email: " + e.getMessage());
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
            content.append("  ").append(supplement.getDescription()).append("\n");
            content.append("  Ссылка: ").append(supplement.getProductUrl()).append("\n\n");
        }
        
        // Дополнительные рекомендации
        if (!additionalRecommendations.isEmpty()) {
            content.append("ДОПОЛНИТЕЛЬНЫЕ РЕКОМЕНДАЦИИ:\n");
            content.append("==============================\n");
            
            for (Supplement supplement : additionalRecommendations) {
                content.append("• ").append(supplement.getName()).append("\n");
                content.append("  ").append(supplement.getDescription()).append("\n");
                content.append("  Ссылка: ").append(supplement.getProductUrl()).append("\n\n");
            }
        }
        
        content.append("Для заказа переходите на наш сайт: https://soloways.ru\n\n");
        content.append("С уважением,\n");
        content.append("Команда SOLOWAYS\n");
        content.append("https://soloways.ru");
        
        return content.toString();
    }

    /**
     * Строит HTML содержимое email
     */
    private String buildHtmlEmailContent(String userName, String selectedTopic, 
                                       List<Supplement> mainRecommendations, 
                                       List<Supplement> additionalRecommendations) {
        try {
            // Загружаем HTML шаблон
            Resource resource = new ClassPathResource("templates/email-recommendations.html");
            String htmlTemplate = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            // Заменяем плейсхолдеры
            htmlTemplate = htmlTemplate.replace("{userName}", userName);
            htmlTemplate = htmlTemplate.replace("{selectedTopic}", getTopicDisplayName(selectedTopic));
            
            // Генерируем HTML для основных рекомендаций
            String mainRecommendationsHtml = buildRecommendationsHtml(mainRecommendations, 3);
            htmlTemplate = htmlTemplate.replace("{mainRecommendations}", mainRecommendationsHtml);
            
            // Генерируем HTML для дополнительных рекомендаций
            String additionalRecommendationsHtml = buildRecommendationsHtml(additionalRecommendations, 2);
            htmlTemplate = htmlTemplate.replace("{additionalRecommendations}", additionalRecommendationsHtml);
            
            return htmlTemplate;
            
        } catch (IOException e) {
            System.err.println("❌ Ошибка загрузки HTML шаблона: " + e.getMessage());
            e.printStackTrace();
            return buildEmailContent(userName, selectedTopic, mainRecommendations, additionalRecommendations);
        }
    }

    /**
     * Строит HTML для рекомендаций
     */
    private String buildRecommendationsHtml(List<Supplement> recommendations, int maxCount) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "";
        }
        
        int count = Math.min(maxCount, recommendations.size());
        StringBuilder html = new StringBuilder();
        
        // Начало блока с товарами
        html.append("<!-- Начало блока с товарами -->");
        html.append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;max-width:600px;Margin:0 auto;\">");
        html.append("<tr>");
        
        // Генерируем ячейки для каждого товара
        for (int i = 0; i < count; i++) {
            Supplement supplement = recommendations.get(i);
            html.append(buildSupplementCardHtml(supplement, i, count));
        }
        
        html.append("</tr>");
        html.append("</table>");
        html.append("<!-- Конец блока -->");
        
        return html.toString();
    }

    /**
     * Строит HTML карточки для одного БАДа
     */
    private String buildSupplementCardHtml(Supplement supplement, int index, int totalCount) {
        String imageUrl = supplement.getImageUrl() != null ? supplement.getImageUrl() : "https://ewunnow.stripocdn.email/content/guids/CABINET_9792b212c76b5f87196ee439d52ce7525ccfe0e78e0e5256a0d822ee48f60855/images/63283055_mzX.jpg";
        String productUrl = supplement.getProductUrl() != null ? supplement.getProductUrl() : "https://soloways.tilda.ws";
        String name = supplement.getName() != null ? supplement.getName() : "БАД";
        String fullDescription = supplement.getDescription() != null ? supplement.getDescription() : "Описание отсутствует";
        String description = truncateDescription(fullDescription, 80); // Ограничиваем до 80 символов
        
        // Определяем ширину ячейки в зависимости от количества товаров
        String cellWidth = totalCount == 3 ? "33.3%" : "50%";
        
        StringBuilder html = new StringBuilder();
        
        // Ячейка товара
        html.append("<!-- Товар ").append(index + 1).append(" -->");
        html.append("<td align=\"center\" valign=\"top\" class=\"stack-column\" style=\"width:").append(cellWidth).append(";padding:10px;\">");
        html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" width=\"100%\">");
        
        // Изображение
        html.append("<tr>");
        html.append("<td align=\"center\">");
        html.append("<img src=\"").append(imageUrl).append("\" alt=\"").append(name).append("\" width=\"150\" style=\"display:block;Margin:0 auto;\">");
        html.append("</td>");
        html.append("</tr>");
        
        // Название
        html.append("<tr>");
        html.append("<td align=\"center\" style=\"font-family:Arial, sans-serif;font-size:14px;color:#333333;padding:5px 0;\">");
        html.append("<strong>").append(name).append("</strong>");
        html.append("</td>");
        html.append("</tr>");
        
        // Описание
        html.append("<tr>");
        html.append("<td align=\"center\" style=\"font-family:Arial, sans-serif;font-size:12px;color:#666666;padding:0 5px;\">");
        html.append(description);
        html.append("</td>");
        html.append("</tr>");
        
        // Кнопка
        html.append("<tr>");
        html.append("<td align=\"center\" style=\"padding:10px 0;\">");
        html.append("<a href=\"").append(productUrl).append("\" target=\"_blank\" style=\"background:#2CB543;color:#ffffff;font-family:Arial,sans-serif;font-size:13px;text-decoration:none;padding:8px 16px;display:inline-block;border-radius:3px;\">");
        html.append("Купить");
        html.append("</a>");
        html.append("</td>");
        html.append("</tr>");
        
        html.append("</table>");
        html.append("</td>");
        
        return html.toString();
    }

    /**
     * Обрезает описание до указанной длины
     */
    private String truncateDescription(String description, int maxLength) {
        if (description == null || description.length() <= maxLength) {
            return description;
        }
        
        // Ищем последний пробел перед максимальной длиной
        String truncated = description.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');
        
        if (lastSpace > maxLength * 0.7) { // Если пробел не слишком далеко от конца
            truncated = truncated.substring(0, lastSpace);
        }
        
        return truncated + "...";
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
