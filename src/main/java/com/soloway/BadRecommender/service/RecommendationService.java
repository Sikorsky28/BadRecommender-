package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Question;
import com.soloway.BadRecommender.model.UserAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final GoogleSheetsDataService googleSheetsDataService;
    private final ScoreCalculationService scoreCalculationService;
    
    // Маппинг между кодами тем и их названиями в Google Sheets
    private static final Map<String, String> TOPIC_MAPPING = createTopicMapping();
    
    // Обратный маппинг для поиска кода темы по русскому названию
    private static final Map<String, String> REVERSE_TOPIC_MAPPING = createReverseTopicMapping();
    
    private static Map<String, String> createTopicMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("energy", "Бодрость и энергия");
        mapping.put("sleep", "Крепкий сон, меньше стресса");
        mapping.put("weight", "Контроль веса и аппетита");
        mapping.put("skin", "Чистая кожа, крепкие волосы");
        mapping.put("digestion", "Комфорт пищеварения");
        mapping.put("joints", "Подвижные суставы, крепкие кости");
        mapping.put("immunity", "Сильный иммунитет");
        mapping.put("heart", "Здоровое сердце и сосуды");
        mapping.put("thyroid", "Поддержка щитовидной железы");
        mapping.put("female", "Регулярный цикл, мягкий ПМС");
        mapping.put("menopause", "Менопауза без приливов");
        mapping.put("male", "Мужское здоровье");
        mapping.put("iron", "Поднять гемоглобин");
        mapping.put("focus", "Фокус и память");
        return mapping;
    }
    
    private static Map<String, String> createReverseTopicMapping() {
        Map<String, String> reverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : TOPIC_MAPPING.entrySet()) {
            reverseMapping.put(entry.getValue(), entry.getKey());
        }
        return reverseMapping;
    }

    @Autowired
    public RecommendationService(GoogleSheetsDataService googleSheetsDataService,
                                ScoreCalculationService scoreCalculationService) {
        this.googleSheetsDataService = googleSheetsDataService;
        this.scoreCalculationService = scoreCalculationService;
    }

    /**
     * Получает список всех доступных категорий (тем)
     */
    public List<String> getAvailableTopics() throws IOException {
        // Возвращаем русские названия тем из Google Sheets
        return googleSheetsDataService.loadCategories();
    }

    /**
     * Получает вопросы для конкретной категории
     */
    public List<Question> getQuestionsByTopic(String topicName) throws IOException {
        // topicName теперь это русское название темы из Google Sheets
        System.out.println("🔍 Ищем вопросы для категории: " + topicName);
        List<GoogleSheetsDataService.Question> specificQuestions = 
            googleSheetsDataService.getQuestionsByCategory(topicName);
        
        System.out.println("✅ Созданы специфичные вопросы для темы '" + topicName + "': " + specificQuestions.size() + " вопросов");
        
        // Загружаем общие вопросы
        List<GoogleSheetsDataService.Question> generalQuestions = 
            googleSheetsDataService.getQuestionsByCategory("Общие вопросы");
        
        System.out.println("✅ Созданы общие вопросы: " + generalQuestions.size() + " вопросов");
        
        // Объединяем специфичные и общие вопросы
        List<GoogleSheetsDataService.Question> allQuestions = new ArrayList<>();
        allQuestions.addAll(specificQuestions);
        allQuestions.addAll(generalQuestions);
        
        System.out.println("   Найдено вопросов: " + allQuestions.size());
        System.out.println("   - Специфичных: " + specificQuestions.size());
        System.out.println("   - Общих: " + generalQuestions.size());
        
        // Конвертируем в модель Question
        return allQuestions.stream()
            .map(this::convertToQuestion)
            .collect(Collectors.toList());
    }

    /**
     * Генерирует рекомендации на основе ответов пользователя
     */
    public ScoreCalculationService.RecommendationResult generateAdvancedRecommendations(
            List<UserAnswer> userAnswers, String selectedTopic) throws IOException {
        // selectedTopic теперь это русское название темы из Google Sheets
        System.out.println("🔍 Генерируем рекомендации для темы: " + selectedTopic);
        return scoreCalculationService.calculateScores(userAnswers, selectedTopic);
    }

    /**
     * Конвертирует GoogleSheetsDataService.Question в модель Question
     */
    private Question convertToQuestion(GoogleSheetsDataService.Question gsQuestion) {
        Question question = new Question();
        question.setId(gsQuestion.getId());
        question.setText(gsQuestion.getText());
        
        // Парсим опции из строки
        if (gsQuestion.getOptions() != null && !gsQuestion.getOptions().isEmpty()) {
            String[] optionsArray = gsQuestion.getOptions().split("/");
            List<String> options = java.util.Arrays.stream(optionsArray)
                .map(String::trim)
                .filter(option -> !option.isEmpty())
                .collect(Collectors.toList());
            question.setOptions(options);
        }
        
        // Устанавливаем тип вопроса
        if ("checkbox".equalsIgnoreCase(gsQuestion.getType())) {
            question.setType(com.soloway.BadRecommender.model.enums.QuestionType.FACTOR);
        } else if ("radio".equalsIgnoreCase(gsQuestion.getType())) {
            question.setType(com.soloway.BadRecommender.model.enums.QuestionType.FACTOR);
        } else {
            question.setType(com.soloway.BadRecommender.model.enums.QuestionType.FACTOR);
        }
        
        question.setRelevant(true);
        question.setGender(null);
        question.setMinAge(null);
        question.setEffects(null);
        
        return question;
  }
}

