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
        
        // Создаем общую таблицу с колонками
        StringBuilder html = new StringBuilder();
        html.append("<!--[if mso]><table style=\"width:560px\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        
        // Добавляем колонки для MSO
        for (int i = 0; i < count; i++) {
            html.append("<td style=\"width:").append(560 / count).append("px\" valign=\"top\">");
        }
        html.append("</tr></table><![endif]-->");
        
        // Основная таблица
        html.append("<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" role=\"none\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">");
        html.append("<tr>");
        
        // Генерируем карточки
        for (int i = 0; i < count; i++) {
            Supplement supplement = recommendations.get(i);
            html.append(buildSupplementCardHtml(supplement));
            
            if (i < count - 1) {
                html.append("<td class=\"es-hidden\" style=\"padding:0;Margin:0;width:20px\"></td>");
            }
        }
        
        html.append("</tr>");
        html.append("</table>");
        
        return html.toString();
    }

    /**
     * Строит HTML карточки для одного БАДа
     */
    private String buildSupplementCardHtml(Supplement supplement) {
        String imageUrl = supplement.getImageUrl() != null ? supplement.getImageUrl() : "https://ewunnow.stripocdn.email/content/guids/CABINET_9792b212c76b5f87196ee439d52ce7525ccfe0e78e0e5256a0d822ee48f60855/images/63283055_mzX.jpg";
        String productUrl = supplement.getProductUrl() != null ? supplement.getProductUrl() : "https://soloways.tilda.ws";
        String name = supplement.getName() != null ? supplement.getName() : "БАД";
        String fullDescription = supplement.getDescription() != null ? supplement.getDescription() : "Описание отсутствует";
        String description = truncateDescription(fullDescription, 80); // Ограничиваем до 80 символов
        
        return String.format(
            "<td align=\"center\" class=\"es-m-p0r es-m-p20b\" style=\"padding:0;Margin:0;width:174px\">" +
            "<table width=\"174\" cellpadding=\"0\" cellspacing=\"0\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:separate;border-spacing:0px;border-left:1px solid #efefef;border-right:1px solid #efefef;border-top:1px solid #efefef;border-bottom:1px solid #efefef;border-radius:5px\" role=\"presentation\">" +
            "<tr>" +
            "<td align=\"center\" style=\"padding:5px;Margin:0;font-size:0px\"><img src=\"%s\" alt=\"\" width=\"164\" height=\"164\" class=\"adapt-img\" style=\"display:block;font-size:14px;border:0;outline:none;text-decoration:none;margin:0\"></td>" +
            "</tr>" +
            "<tr>" +
            "<td align=\"center\" style=\"padding:0;Margin:0;padding-right:10px;padding-left:10px\"><h3 class=\"es-m-txt-c\" style=\"Margin:0;font-family:helvetica, 'helvetica neue', arial, verdana, sans-serif;mso-line-height-rule:exactly;letter-spacing:0;font-size:20px;font-style:normal;font-weight:bold;line-height:24px;color:#333333\">%s</h3><p class=\"es-m-txt-c\" style=\"Margin:0;mso-line-height-rule:exactly;font-family:helvetica, 'helvetica neue', arial, verdana, sans-serif;line-height:21px;letter-spacing:0;color:#333333;font-size:14px\"><br></p></td>" +
            "</tr>" +
            "<tr>" +
            "<td align=\"center\" style=\"padding:0;Margin:0;padding-top:5px;padding-right:10px;padding-left:10px\"><p style=\"Margin:0;mso-line-height-rule:exactly;font-family:merriweather, georgia, 'times new roman', serif;line-height:14.4px;letter-spacing:0;color:#333333;font-size:12px\">%s</p><p style=\"Margin:0;mso-line-height-rule:exactly;font-family:merriweather, georgia, 'times new roman', serif;line-height:14.4px;letter-spacing:0;color:#333333;font-size:12px\"><br></p></td>" +
            "</tr>" +
            "<tr>" +
            "<td align=\"center\" style=\"padding:0;Margin:0;padding-bottom:20px;padding-right:5px;padding-left:5px\"><span class=\"es-button-border\" style=\"border-style:solid;border-color:#5c68e2;background:#55685b;border-width:0;display:inline-block;border-radius:8px;width:auto\"><a href=\"%s\" target=\"_blank\" class=\"es-button es-button-1621628636490\" style=\"mso-style-priority:100 !important;text-decoration:none !important;mso-line-height-rule:exactly;color:#ffffff;font-size:20px;padding:5px 30px;display:inline-block;background:#55685b;border-radius:8px;font-family:arial, 'helvetica neue', helvetica, sans-serif;font-weight:normal;font-style:normal;line-height:24px;width:auto;text-align:center;letter-spacing:0;mso-padding-alt:0;mso-border-alt:10px solid #55685b\">купить</a></span></td>" +
            "</tr>" +
            "</table></td>",
            imageUrl, name, description, productUrl
        );
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
