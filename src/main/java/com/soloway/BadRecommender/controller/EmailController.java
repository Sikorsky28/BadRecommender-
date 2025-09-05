package com.soloway.BadRecommender.controller;

import com.soloway.BadRecommender.model.Supplement;
import com.soloway.BadRecommender.service.EmailService;
import com.soloway.BadRecommender.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Отправляет email с рекомендациями на основе данных пользователя
     */
    @PostMapping("/send-recommendations")
    public ResponseEntity<?> sendRecommendationsEmail(@RequestBody EmailRequest request) {
        try {
            // Получаем рекомендации на основе выбранной темы
            List<Supplement> mainRecommendations = recommendationService.getMainRecommendations(request.getSelectedTopic());
            List<Supplement> additionalRecommendations = recommendationService.getAdditionalRecommendations(request.getSelectedTopic());

            // Отправляем HTML email
            emailService.sendHtmlRecommendationsEmail(
                request.getUserEmail(),
                request.getUserName(),
                request.getSelectedTopic(),
                mainRecommendations,
                additionalRecommendations
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email с рекомендациями успешно отправлен на " + request.getUserEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Ошибка отправки email: " + e.getMessage()
            ));
        }
    }

    /**
     * Тестовый endpoint для отправки email
     */
    @PostMapping("/test")
    public ResponseEntity<?> testEmail(@RequestBody TestEmailRequest request) {
        try {
            emailService.sendEmail(request.getEmail(), "Тестовое письмо", "Это тестовое письмо от SOLOWAYS");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Тестовое письмо отправлено на " + request.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Ошибка отправки тестового письма: " + e.getMessage()
            ));
        }
    }

    /**
     * Класс для запроса отправки email с рекомендациями
     */
    public static class EmailRequest {
        private String userEmail;
        private String userName;
        private String selectedTopic;

        // Геттеры и сеттеры
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getSelectedTopic() { return selectedTopic; }
        public void setSelectedTopic(String selectedTopic) { this.selectedTopic = selectedTopic; }
    }

    /**
     * Класс для тестового запроса
     */
    public static class TestEmailRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
