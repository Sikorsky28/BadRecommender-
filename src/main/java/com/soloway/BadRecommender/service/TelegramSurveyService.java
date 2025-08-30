package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Question;
import com.soloway.BadRecommender.model.TelegramUser;
import com.soloway.BadRecommender.model.UserAnswer;
import com.soloway.BadRecommender.repository.QuestionRepository;
import com.soloway.BadRecommender.service.ScoreCalculationService;
import com.soloway.BadRecommender.service.GoogleSheetsDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.io.IOException;

/**
 * Сервис для управления опросом в Telegram боте
 * Использует ту же логику, что и веб-интерфейс
 */
@Service
public class TelegramSurveyService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramSurveyService.class);

    private final QuestionRepository questionRepository;
    private final RecommendationService recommendationService;
    private final GoogleSheetsDataService googleSheetsDataService;

    // Кэш для тем (загружаем динамически)
    private List<String> cachedTopics = null;
    private long topicsCacheTime = 0;
    
    // Кэш для вопросов по темам
    private Map<String, List<Question>> cachedQuestions = new HashMap<>();
    private Map<String, Long> questionsCacheTime = new HashMap<>();
    
    private static final long CACHE_TTL = 300000; // 5 минут

    public TelegramSurveyService(QuestionRepository questionRepository, RecommendationService recommendationService, GoogleSheetsDataService googleSheetsDataService) {
        this.questionRepository = questionRepository;
        this.recommendationService = recommendationService;
        this.googleSheetsDataService = googleSheetsDataService;
    }

    /**
     * Получить следующий вопрос для пользователя
     */
    public SurveyQuestion getNextQuestion(TelegramUser user) {
        int currentIndex = user.getCurrentQuestionIndex();
        
        // Определяем этап опроса
        if (currentIndex == 0) {
            return getTopicQuestion(user);
        } else {
            // Динамические вопросы по выбранной теме
            return getDynamicQuestionByIndex(user, currentIndex - 1);
        }
    }

    /**
     * Загрузить темы из Google Sheets с кэшированием
     */
    private List<String> loadTopics() {
        long currentTime = System.currentTimeMillis();
        
        // Проверяем кэш
        if (cachedTopics != null && (currentTime - topicsCacheTime) < CACHE_TTL) {
            return cachedTopics;
        }
        
        try {
            // Загружаем темы из Google Sheets
            List<String> topics = googleSheetsDataService.loadCategories();
            cachedTopics = topics;
            topicsCacheTime = currentTime;
            logger.info("Загружено {} тем из Google Sheets", topics.size());
            return topics;
        } catch (IOException e) {
            logger.error("Ошибка загрузки тем из Google Sheets: {}", e.getMessage());
            // Возвращаем кэшированные темы или пустой список
            return cachedTopics != null ? cachedTopics : new ArrayList<>();
        }
    }

    /**
     * Вопрос о выборе темы
     */
    private SurveyQuestion getTopicQuestion(TelegramUser user) {
        List<String> topics = loadTopics();
        
        return SurveyQuestion.builder()
            .text("Здравствуйте, " + (user.getFirstName() != null ? user.getFirstName() : "пользователь") + 
                  ", выберите интересующую вас тему:")
            .options(topics)
            .questionType(QuestionType.TOPIC_SELECTION)
            .build();
    }

    /**
     * Загрузить вопросы для темы с кэшированием
     */
    private List<Question> loadQuestionsForTopic(String topic) {
        long currentTime = System.currentTimeMillis();
        
        // Проверяем кэш
        if (cachedQuestions.containsKey(topic)) {
            Long cacheTime = questionsCacheTime.get(topic);
            if (cacheTime != null && (currentTime - cacheTime) < CACHE_TTL) {
                logger.debug("Вопросы для темы '{}' загружены из кэша", topic);
                return cachedQuestions.get(topic);
            }
        }
        
        try {
            // Загружаем вопросы из Google Sheets
            List<Question> questions = recommendationService.getQuestionsByTopic(topic);
            cachedQuestions.put(topic, questions);
            questionsCacheTime.put(topic, currentTime);
            logger.info("Загружено {} вопросов для темы '{}' из Google Sheets", questions.size(), topic);
            return questions;
        } catch (IOException e) {
            logger.error("Ошибка загрузки вопросов для темы '{}': {}", topic, e.getMessage());
            // Возвращаем кэшированные вопросы или пустой список
            return cachedQuestions.getOrDefault(topic, new ArrayList<>());
        }
    }

    /**
     * Получить конкретный динамический вопрос по индексу
     */
    private SurveyQuestion getDynamicQuestionByIndex(TelegramUser user, int questionIndex) {
        String selectedTopic = user.getSelectedTopic();
        
        // Загружаем вопросы с кэшированием
        List<Question> topicQuestions = loadQuestionsForTopic(selectedTopic);
        
        if (questionIndex >= topicQuestions.size()) {
            return null; // Опрос завершен
        }
        
        Question question = topicQuestions.get(questionIndex);
        return SurveyQuestion.builder()
            .text(question.getText())
            .options(question.getOptions())
            .questionType(QuestionType.DYNAMIC)
            .questionId(question.getId())
            .build();
    }

    /**
     * Обработать ответ пользователя
     */
    public void processAnswer(TelegramUser user, String answer) {
        int currentIndex = user.getCurrentQuestionIndex();
        
        if (currentIndex == 0) {
            // Выбор темы
            String selectedTopic = getTopicByAnswer(answer);
            user.setSelectedTopic(selectedTopic);
            logger.info("Пользователь {} выбрал тему: {}", user.getUsername(), selectedTopic);
        } else {
            // Сохраняем ответ на динамический вопрос
            user.addAnswer(currentIndex - 1, answer);
            logger.info("Пользователь {} ответил на вопрос {}: {}", user.getUsername(), currentIndex, answer);
        }
        
        user.nextQuestion();
    }

    /**
     * Получить тему по ответу пользователя
     */
    private String getTopicByAnswer(String answer) {
        // Теперь тема - это просто название из Google Sheets
        // Возвращаем название темы как есть
        return answer;
    }

    /**
     * Проверить, завершен ли опрос
     */
    public boolean isSurveyCompleted(TelegramUser user) {
        String selectedTopic = user.getSelectedTopic();
        if (selectedTopic == null) {
            return false;
        }
        
        List<Question> topicQuestions = loadQuestionsForTopic(selectedTopic);
        int totalQuestions = 1 + topicQuestions.size(); // 1 для выбора темы + вопросы по теме
        
        return user.getCurrentQuestionIndex() >= totalQuestions;
    }

    /**
     * Получить общее количество вопросов для темы
     */
    public int getTotalQuestionsForTopic(String topic) {
        List<Question> questions = loadQuestionsForTopic(topic);
        return 1 + questions.size(); // 1 для выбора темы + вопросы по теме
    }

    /**
     * Получить рекомендации на основе ответов пользователя
     */
    public ScoreCalculationService.RecommendationResult getRecommendations(TelegramUser user) {
        String selectedTopic = user.getSelectedTopic();
        List<UserAnswer> userAnswers = new ArrayList<>();
        
        try {
            // Преобразуем ответы пользователя в формат UserAnswer
            Map<Integer, String> answers = user.getAnswers();
            List<Question> questions = loadQuestionsForTopic(selectedTopic);
            
            for (int i = 0; i < questions.size(); i++) {
                String answer = answers.get(i);
                if (answer != null) {
                    Question question = questions.get(i);
                    UserAnswer userAnswer = new UserAnswer();
                    userAnswer.setQuestionId(question.getId());
                    userAnswer.setAnswer(answer);
                    userAnswers.add(userAnswer);
                }
            }
            
            return recommendationService.generateAdvancedRecommendations(userAnswers, selectedTopic);
        } catch (IOException e) {
            logger.error("Ошибка получения рекомендаций для темы {}: {}", selectedTopic, e.getMessage());
            // Возвращаем пустой результат при ошибке
            return new ScoreCalculationService.RecommendationResult(
                new ArrayList<>(), 
                new ArrayList<>()
            );
        }
    }

    /**
     * Класс для представления вопроса опроса
     */
    public static class SurveyQuestion {
        private String text;
        private List<String> options;
        private QuestionType questionType;
        private String questionId;

        private SurveyQuestion(Builder builder) {
            this.text = builder.text;
            this.options = builder.options;
            this.questionType = builder.questionType;
            this.questionId = builder.questionId;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Геттеры
        public String getText() { return text; }
        public List<String> getOptions() { return options; }
        public QuestionType getQuestionType() { return questionType; }
        public String getQuestionId() { return questionId; }

        public static class Builder {
            private String text;
            private List<String> options;
            private QuestionType questionType;
            private String questionId;

            public Builder text(String text) {
                this.text = text;
                return this;
            }

            public Builder options(List<String> options) {
                this.options = options;
                return this;
            }

            public Builder questionType(QuestionType questionType) {
                this.questionType = questionType;
                return this;
            }

            public Builder questionId(String questionId) {
                this.questionId = questionId;
                return this;
            }

            public SurveyQuestion build() {
                return new SurveyQuestion(this);
            }
        }
    }

    /**
     * Типы вопросов
     */
    public enum QuestionType {
        TOPIC_SELECTION,
        DYNAMIC
    }
}
