package com.soloway.BadRecommender.controller;

import com.soloway.BadRecommender.model.UserAnswer;
import com.soloway.BadRecommender.model.Question;
import com.soloway.BadRecommender.model.Supplement;

import com.soloway.BadRecommender.service.RecommendationService;
import com.soloway.BadRecommender.service.RecommendationCalculationService;
import com.soloway.BadRecommender.service.GoogleSheetsService;
import com.soloway.BadRecommender.service.EmailService;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

import java.util.List;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/recommendation")
@CrossOrigin(origins = "*")
public class RecommendationController {

  private final RecommendationService recommendationService;
      private final GoogleSheetsService googleSheetsService;
  private final EmailService emailService;

      public RecommendationController(RecommendationService recommendationService, GoogleSheetsService googleSheetsService, EmailService emailService) {
        this.recommendationService = recommendationService;
        this.googleSheetsService = googleSheetsService;
        this.emailService = emailService;
    }

  // 🔹 Получить все доступные темы здоровья
  @GetMapping("/topics")
  public List<String> getAvailableTopics() {
    return recommendationService.getAvailableTopics();
  }

  // 🔹 Получить вопросы по выбранной теме
  @GetMapping("/questions")
  public List<Question> getQuestionsByTopic(@RequestParam String topics) {
    System.out.println("🔍 Запрос вопросов по теме:");
    System.out.println("   Тема: " + topics);
    
    List<Question> questions = recommendationService.getQuestionsByTopic(topics);
    System.out.println("   Найдено вопросов: " + questions.size());
    
    return questions;
  }

  // 🔹 Основной эндпоинт для получения рекомендаций
  @PostMapping("/submit")
  public RecommendationCalculationService.RecommendationResult submitAnswers(
      @RequestBody RecommendationRequest request) {
    
    System.out.println("🎯 Запрос рекомендаций:");
    System.out.println("   Выбранная тема: " + request.getSelectedTopic());
    System.out.println("   Количество ответов: " + request.getAnswers().size());
    System.out.println("📧 Данные пользователя:");
    System.out.println("   Email: " + request.getEmail());
    System.out.println("   Согласие на обработку ПД: " + request.isConsentPd());
    System.out.println("   Согласие на маркетинг: " + request.isConsentMarketing());
    
    RecommendationCalculationService.RecommendationResult result = 
        recommendationService.generateAdvancedRecommendations(request.getAnswers(), request.getSelectedTopic());
    
    System.out.println("   Основных рекомендаций: " + result.getMainRecommendations().size());
    System.out.println("   Дополнительных рекомендаций: " + result.getAdditionalRecommendations().size());
    
    // Отправляем email с рекомендациями, если предоставлен email и дано согласие
    if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && request.isConsentPd()) {
        try {
            // Получаем имя пользователя из запроса
            String userName = request.getUserName() != null ? request.getUserName() : "Пользователь";
            
            emailService.sendRecommendationsEmail(
                request.getEmail(),
                userName,
                request.getSelectedTopic(),
                result.getMainRecommendations(),
                result.getAdditionalRecommendations()
            );
            
            System.out.println("📧 Email с рекомендациями отправлен на: " + request.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки email: " + e.getMessage());
            // Не прерываем выполнение, если email не отправился
        }
    } else {
        System.out.println("📧 Email не отправлен: отсутствует email или согласие на обработку ПД");
    }
    
    return result;
  }

  // 🔹 Тестовый эндпоинт для демонстрации работы системы
  @GetMapping("/demo")
  public RecommendationCalculationService.RecommendationResult getDemoRecommendations() {
    System.out.println("🧪 Демонстрационный запрос рекомендаций");
    
    // Создаем тестовые данные
    String selectedTopic = "energy";
    List<UserAnswer> answers = new ArrayList<>();
    
    UserAnswer answer1 = new UserAnswer();
    answer1.setQuestionId("morning_energy");
    answer1.setAnswer("почти всегда");
    answers.add(answer1);
    
    UserAnswer answer2 = new UserAnswer();
    answer2.setQuestionId("afternoon_crash");
    answer2.setAnswer("почти всегда");
    answers.add(answer2);
    
    UserAnswer answer3 = new UserAnswer();
    answer3.setQuestionId("post_infection_fatigue");
    answer3.setAnswer("да, держится");
    answers.add(answer3);
    
    UserAnswer answer4 = new UserAnswer();
    answer4.setQuestionId("exercise_fatigue");
    answer4.setAnswer("почти всегда");
    answers.add(answer4);
    
    UserAnswer answer5 = new UserAnswer();
    answer5.setQuestionId("iron_anemia_doctor");
    answer5.setAnswer("да");
    answers.add(answer5);
    
    UserAnswer answer6 = new UserAnswer();
    answer6.setQuestionId("caffeine_sensitivity");
    answer6.setAnswer("часто");
    answers.add(answer6);
    
    RecommendationCalculationService.RecommendationResult result = 
        recommendationService.generateAdvancedRecommendations(answers, selectedTopic);
    
    System.out.println("   Основных рекомендаций: " + result.getMainRecommendations().size());
    System.out.println("   Дополнительных рекомендаций: " + result.getAdditionalRecommendations().size());
    
    return result;
  }

  // 🔹 Тестовый эндпоинт для проверки описаний
  @GetMapping("/test-descriptions")
  public List<Supplement> testDescriptions() {
    System.out.println("🧪 Тестируем описания добавок");
    
    try {
      List<Supplement> supplements = googleSheetsService.loadSupplements();
      System.out.println("📋 Найдено добавок: " + supplements.size());
      
      for (Supplement supplement : supplements) {
        System.out.println("📝 " + supplement.getName() + ": " + supplement.getDescription());
      }
      
      return supplements;
    } catch (IOException e) {
      System.err.println("❌ Ошибка загрузки из Google Sheets: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>();
    } catch (Exception e) {
      System.err.println("❌ Общая ошибка: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  // 🔹 Класс для запроса рекомендаций
  public static class RecommendationRequest {
    private String selectedTopic;
    private List<UserAnswer> answers;
    private String email;
    private boolean consentPd;
    private boolean consentMarketing;
    private String userName;

    public String getSelectedTopic() { return selectedTopic; }
    public void setSelectedTopic(String selectedTopic) { this.selectedTopic = selectedTopic; }

    public List<UserAnswer> getAnswers() { return answers; }
    public void setAnswers(List<UserAnswer> answers) { this.answers = answers; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isConsentPd() { return consentPd; }
    public void setConsentPd(boolean consentPd) { this.consentPd = consentPd; }

    public boolean isConsentMarketing() { return consentMarketing; }
    public void setConsentMarketing(boolean consentMarketing) { this.consentMarketing = consentMarketing; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
  }
}
