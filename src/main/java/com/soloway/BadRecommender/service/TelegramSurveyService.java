package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Question;
import com.soloway.BadRecommender.model.TelegramUser;
import com.soloway.BadRecommender.model.UserAnswer;
import com.soloway.BadRecommender.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Сервис для управления опросом в Telegram боте
 * Использует ту же логику, что и веб-интерфейс
 */
@Service
public class TelegramSurveyService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramSurveyService.class);

    private final QuestionRepository questionRepository;
    private final RecommendationService recommendationService;

    // Темы опроса
    private static final Map<String, String> TOPICS = createTopicsMap();

    private static Map<String, String> createTopicsMap() {
        Map<String, String> topics = new HashMap<>();
        topics.put("energy", "Бодрость и энергия");
        topics.put("sleep", "Крепкий сон, меньше стресса");
        topics.put("weight", "Контроль веса и аппетита");
        topics.put("skin", "Чистая кожа, крепкие волосы");
        topics.put("digestion", "Комфорт пищеварения");
        topics.put("joints", "Подвижные суставы, крепкие кости");
        topics.put("immunity", "Сильный иммунитет");
        topics.put("heart", "Здоровое сердце и сосуды");
        topics.put("thyroid", "Поддержка щитовидной железы");
        topics.put("female", "Регулярный цикл, мягкий ПМС");
        topics.put("menopause", "Менопауза без приливов");
        topics.put("male", "Мужское здоровье");
        topics.put("iron", "Поднять гемоглобин");
        return topics;
    }

    public TelegramSurveyService(QuestionRepository questionRepository, RecommendationService recommendationService) {
        this.questionRepository = questionRepository;
        this.recommendationService = recommendationService;
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
     * Вопрос о выборе темы
     */
    private SurveyQuestion getTopicQuestion(TelegramUser user) {
        List<String> options = new ArrayList<>(TOPICS.values());
        
        return SurveyQuestion.builder()
            .text("Здравствуйте, " + (user.getFirstName() != null ? user.getFirstName() : "пользователь") + 
                  ", выберите интересующую вас тему:")
            .options(options)
            .questionType(QuestionType.TOPIC_SELECTION)
            .build();
    }

    /**
     * Получить конкретный динамический вопрос по индексу
     */
    private SurveyQuestion getDynamicQuestionByIndex(TelegramUser user, int questionIndex) {
        String selectedTopic = user.getSelectedTopic();
        List<Question> topicQuestions = recommendationService.getQuestionsByTopic(selectedTopic);
        
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
        for (Map.Entry<String, String> entry : TOPICS.entrySet()) {
            if (entry.getValue().equals(answer)) {
                return entry.getKey();
            }
        }
        return "energy"; // По умолчанию
    }

    /**
     * Проверить, завершен ли опрос
     */
    public boolean isSurveyCompleted(TelegramUser user) {
        String selectedTopic = user.getSelectedTopic();
        if (selectedTopic == null) {
            return false;
        }
        
        List<Question> topicQuestions = recommendationService.getQuestionsByTopic(selectedTopic);
        int totalQuestions = 1 + topicQuestions.size(); // 1 для выбора темы + вопросы по теме
        
        return user.getCurrentQuestionIndex() >= totalQuestions;
    }

    /**
     * Получить общее количество вопросов для темы
     */
    public int getTotalQuestionsForTopic(String topic) {
        List<Question> questions = recommendationService.getQuestionsByTopic(topic);
        return 1 + questions.size(); // 1 для выбора темы + вопросы по теме
    }

    /**
     * Получить рекомендации на основе ответов пользователя
     */
    public RecommendationCalculationService.RecommendationResult getRecommendations(TelegramUser user) {
        String selectedTopic = user.getSelectedTopic();
        List<UserAnswer> userAnswers = new ArrayList<>();
        
        // Преобразуем ответы пользователя в формат UserAnswer
        Map<Integer, String> answers = user.getAnswers();
        List<Question> questions = recommendationService.getQuestionsByTopic(selectedTopic);
        
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
