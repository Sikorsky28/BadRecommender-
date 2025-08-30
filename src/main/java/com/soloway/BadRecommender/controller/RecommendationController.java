package com.soloway.BadRecommender.controller;

import com.soloway.BadRecommender.model.UserAnswer;
import com.soloway.BadRecommender.model.Question;
import com.soloway.BadRecommender.model.Supplement;

import com.soloway.BadRecommender.service.RecommendationService;
import com.soloway.BadRecommender.service.ScoreCalculationService;
import com.soloway.BadRecommender.service.GoogleSheetsService;
import com.soloway.BadRecommender.service.GoogleSheetsDataService;
import com.soloway.BadRecommender.service.EmailService;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/recommendation")
@CrossOrigin(origins = "*")
public class RecommendationController {

  private final RecommendationService recommendationService;
      private final GoogleSheetsService googleSheetsService;
  private final GoogleSheetsDataService googleSheetsDataService;
  private final EmailService emailService;

      public RecommendationController(RecommendationService recommendationService, GoogleSheetsService googleSheetsService, GoogleSheetsDataService googleSheetsDataService, EmailService emailService) {
        this.recommendationService = recommendationService;
        this.googleSheetsService = googleSheetsService;
        this.googleSheetsDataService = googleSheetsDataService;
        this.emailService = emailService;
    }

  // 🔹 Получить все доступные темы здоровья
  @GetMapping("/topics")
  public List<String> getAvailableTopics() {
    try {
      return recommendationService.getAvailableTopics();
    } catch (IOException e) {
      System.err.println("❌ Ошибка получения тем: " + e.getMessage());
      // Возвращаем пустой список при ошибке
      return new ArrayList<>();
    }
  }

  // 🔹 Получить вопросы по выбранной теме
  @GetMapping("/questions")
  public List<Question> getQuestionsByTopic(@RequestParam String topics) {
    System.out.println("🔍 Запрос вопросов по теме:");
    System.out.println("   Тема: " + topics);
    
    try {
      List<Question> questions = recommendationService.getQuestionsByTopic(topics);
      System.out.println("   Найдено вопросов: " + questions.size());
      return questions;
    } catch (IOException e) {
      System.err.println("❌ Ошибка получения вопросов: " + e.getMessage());
      // Возвращаем пустой список при ошибке
      return new ArrayList<>();
    }
  }

  // 🔹 Основной эндпоинт для получения рекомендаций
  @PostMapping("/submit")
  public ScoreCalculationService.RecommendationResult submitAnswers(
      @RequestBody RecommendationRequest request) {
    
    System.out.println("🎯 Запрос рекомендаций:");
    System.out.println("   Выбранная тема: " + request.getSelectedTopic());
    System.out.println("   Количество ответов: " + request.getAnswers().size());
    System.out.println("📧 Данные пользователя:");
    System.out.println("   Email: " + request.getEmail());
    System.out.println("   Согласие на обработку ПД: " + request.isConsentPd());
    System.out.println("   Согласие на маркетинг: " + request.isConsentMarketing());
    
    ScoreCalculationService.RecommendationResult result;
    try {
        result = recommendationService.generateAdvancedRecommendations(request.getAnswers(), request.getSelectedTopic());
    } catch (IOException e) {
        System.err.println("❌ Ошибка генерации рекомендаций: " + e.getMessage());
        // Возвращаем пустой результат при ошибке
        result = new ScoreCalculationService.RecommendationResult(
            new ArrayList<>(), 
            new ArrayList<>()
        );
    }
    
    System.out.println("   Основных рекомендаций: " + result.getMainRecommendations().size());
    System.out.println("   Дополнительных рекомендаций: " + result.getAdditionalRecommendations().size());
    
    // Отправляем email с рекомендациями, если предоставлен email и дано согласие
    if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && request.isConsentPd()) {
        try {
            // Получаем имя пользователя из запроса
            String userName = request.getUserName() != null ? request.getUserName() : "Пользователь";
            
            // Конвертируем SupplementWithScore в Supplement для EmailService
            List<Supplement> mainSupplements = convertToSupplements(result.getMainRecommendations());
            List<Supplement> additionalSupplements = convertToSupplements(result.getAdditionalRecommendations());
            
            emailService.sendRecommendationsEmail(
                request.getEmail(),
                userName,
                request.getSelectedTopic(),
                mainSupplements,
                additionalSupplements
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
  public ScoreCalculationService.RecommendationResult getDemoRecommendations() throws IOException {
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
    
    ScoreCalculationService.RecommendationResult result = 
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

  // 🔹 Тестовый эндпоинт для проверки правил AnswerScores
  @GetMapping("/test-answer-scores")
  public List<Object> testAnswerScores() {
    System.out.println("🧪 Тестируем правила AnswerScores");
    
    try {
      List<GoogleSheetsDataService.AnswerScore> answerScores = googleSheetsDataService.loadAnswerScores();
      System.out.println("📊 Найдено правил: " + answerScores.size());
      
      List<Object> result = new ArrayList<>();
      for (GoogleSheetsDataService.AnswerScore score : answerScores) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("questionId", score.getQuestionId());
        rule.put("answer", score.getAnswer());
        rule.put("supplementCode", score.getSupplementCode());
        rule.put("score", score.getScore());
        rule.put("description", score.getDescription());
        result.add(rule);
      }
      
      return result;
    } catch (IOException e) {
      System.err.println("❌ Ошибка загрузки AnswerScores: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  // 🔹 Тестовый эндпоинт для диагностики Google Sheets
  @GetMapping("/test-sheets-diagnostic")
  public Map<String, Object> testSheetsDiagnostic() {
    System.out.println("🔍 Диагностика Google Sheets");
    
    Map<String, Object> diagnostic = new HashMap<>();
    
    try {
      // Проверяем лист AnswerScores
      List<GoogleSheetsDataService.AnswerScore> answerScores = googleSheetsDataService.loadAnswerScores();
      diagnostic.put("answerScoresCount", answerScores.size());
      diagnostic.put("answerScoresLoaded", !answerScores.isEmpty());
      
      // Проверяем лист BaseScores
      List<GoogleSheetsDataService.BaseScore> baseScores = googleSheetsDataService.loadBaseScores();
      diagnostic.put("baseScoresCount", baseScores.size());
      diagnostic.put("baseScoresLoaded", !baseScores.isEmpty());
      
      // Проверяем лист Categories
      List<String> categories = googleSheetsDataService.loadCategories();
      diagnostic.put("categoriesCount", categories.size());
      diagnostic.put("categoriesLoaded", !categories.isEmpty());
      
      // Проверяем лист Questions
      List<GoogleSheetsDataService.Question> questions = googleSheetsDataService.loadQuestions();
      diagnostic.put("questionsCount", questions.size());
      diagnostic.put("questionsLoaded", !questions.isEmpty());
      
      diagnostic.put("status", "success");
      
    } catch (Exception e) {
      diagnostic.put("status", "error");
      diagnostic.put("error", e.getMessage());
      e.printStackTrace();
    }
    
    return diagnostic;
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

  /**
   * Конвертирует SupplementWithScore в Supplement для EmailService
   */
  private List<Supplement> convertToSupplements(List<ScoreCalculationService.SupplementWithScore> supplementWithScores) {
    List<Supplement> supplements = new ArrayList<>();
    
    try {
      // Для каждого SupplementWithScore извлекаем Supplement
      for (ScoreCalculationService.SupplementWithScore supplementWithScore : supplementWithScores) {
        Supplement supplement = supplementWithScore.getSupplement();
        if (supplement != null) {
          supplements.add(supplement);
        }
      }
    } catch (Exception e) {
      System.err.println("❌ Ошибка конвертации SupplementWithScore в Supplement: " + e.getMessage());
    }
    
    return supplements;
  }
}
