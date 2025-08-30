package com.soloway.BadRecommender.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsDataService {

    private final Sheets sheetsService;
    private final String spreadsheetId;

    // Названия листов в Google Sheets (используем ваши существующие)
    private static final String CATEGORIES_SHEET = "Categories";
    private static final String QUESTIONS_SHEET = "Questions";
    private static final String ANSWER_SCORES_SHEET = "AnswerScores";
    private static final String BASE_SCORES_SHEET = "BaseScores";
    private static final String SUPPLEMENTS_SHEET = "supplements";

    @Autowired
    public GoogleSheetsDataService(Sheets sheetsService,
                                   @Value("${google.sheets.spreadsheet-id}") String spreadsheetId) {
        this.sheetsService = sheetsService;
        this.spreadsheetId = spreadsheetId;
    }

    /**
     * Загружает все категории (темы) из листа Categories
     */
    public List<String> loadCategories() throws IOException {
        System.out.println("📊 Загружаем категории из Google Sheets...");
        
        try {
            ValueRange range = sheetsService.spreadsheets().values()
                .get(spreadsheetId, CATEGORIES_SHEET + "!A2:B")
                .execute();
            
            List<String> categories = new ArrayList<>();
            
            if (range.getValues() != null) {
                for (List<Object> row : range.getValues()) {
                    if (row.size() >= 2 && row.get(1) != null) {
                        String categoryName = row.get(1).toString().trim();
                        if (!categoryName.isEmpty()) {
                            categories.add(categoryName);
                        }
                    }
                }
            }
            
            System.out.println("✅ Загружено категорий: " + categories.size());
            return categories;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки категорий: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Загружает все вопросы из листа Questions
     */
    public List<Question> loadQuestions() throws IOException {
        System.out.println("📊 Загружаем вопросы из Google Sheets...");
        
        try {
            ValueRange range = sheetsService.spreadsheets().values()
                .get(spreadsheetId, QUESTIONS_SHEET + "!A2:E")
                .execute();
            
            List<Question> questions = new ArrayList<>();
            
            if (range.getValues() != null) {
                for (List<Object> row : range.getValues()) {
                    if (row.size() >= 5) {
                        try {
                            Question question = new Question();
                            question.setId(row.get(0) != null ? row.get(0).toString() : "");
                            question.setCategory(row.get(1) != null ? row.get(1).toString() : "");
                            question.setText(row.get(2) != null ? row.get(2).toString() : "");
                            question.setType(row.get(3) != null ? row.get(3).toString() : "");
                            question.setOptions(row.get(4) != null ? row.get(4).toString() : "");
                            
                            questions.add(question);
                        } catch (Exception e) {
                            System.err.println("❌ Ошибка парсинга вопроса: " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("✅ Загружено вопросов: " + questions.size());
            return questions;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки вопросов: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Загружает правила начисления баллов из листа AnswerScores
     */
    public List<AnswerScore> loadAnswerScores() throws IOException {
        System.out.println("📊 Загружаем правила начисления баллов из Google Sheets...");
        
        try {
            ValueRange range = sheetsService.spreadsheets().values()
                .get(spreadsheetId, ANSWER_SCORES_SHEET + "!A1:E")
                .execute();
            
            List<AnswerScore> answerScores = new ArrayList<>();
            
            if (range.getValues() != null) {
                boolean isFirstRow = true;
                for (List<Object> row : range.getValues()) {
                    // Пропускаем первую строку (заголовки)
                    if (isFirstRow) {
                        isFirstRow = false;
                        continue;
                    }
                    
                    if (row.size() >= 4) { // Минимум 4 колонки: questionId, answer, supplementCode, score
                        try {
                            AnswerScore answerScore = new AnswerScore();
                            answerScore.setQuestionId(row.get(0) != null ? row.get(0).toString().trim() : "");
                            answerScore.setAnswer(row.get(1) != null ? row.get(1).toString().trim() : "");
                            answerScore.setSupplementCode(row.get(2) != null ? row.get(2).toString().trim() : "");
                            
                            // Парсим баллы
                            if (row.get(3) != null) {
                                try {
                                    double score = Double.parseDouble(row.get(3).toString().trim());
                                    answerScore.setScore(score);
                                } catch (NumberFormatException e) {
                                    answerScore.setScore(0.0);
                                }
                            }
                            
                            // Описание может быть пустым
                            answerScore.setDescription(row.size() >= 5 && row.get(4) != null ? row.get(4).toString().trim() : "");
                            
                            // Проверяем, что все обязательные поля заполнены
                            if (!answerScore.getQuestionId().isEmpty() && 
                                !answerScore.getAnswer().isEmpty() && 
                                !answerScore.getSupplementCode().isEmpty()) {
                                answerScores.add(answerScore);
                                System.out.println("✅ Добавлено правило: " + answerScore.getQuestionId() + " -> " + answerScore.getAnswer() + " -> " + answerScore.getSupplementCode() + " = " + answerScore.getScore());
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Ошибка парсинга правила: " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("✅ Загружено правил: " + answerScores.size());
            return answerScores;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки правил: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Получает вопросы для конкретной категории
     */
    public List<Question> getQuestionsByCategory(String categoryName) throws IOException {
        List<Question> allQuestions = loadQuestions();
        
        return allQuestions.stream()
            .filter(q -> q.getCategory().equalsIgnoreCase(categoryName))
            .collect(Collectors.toList());
    }

    /**
     * Получает список всех доступных категорий
     */
    public List<String> getAvailableCategories() throws IOException {
        return loadCategories();
    }

    /**
     * Загружает базовые баллы для добавок по темам из листа BaseScore
     */
    public List<BaseScore> loadBaseScores() throws IOException {
        System.out.println("📊 Загружаем базовые баллы из Google Sheets...");
        
        try {
            ValueRange range = sheetsService.spreadsheets().values()
                .get(spreadsheetId, BASE_SCORES_SHEET + "!A2:D")
                .execute();
            
            List<BaseScore> baseScores = new ArrayList<>();
            
            if (range.getValues() != null) {
                for (List<Object> row : range.getValues()) {
                    if (row.size() >= 4) {
                        try {
                            BaseScore baseScore = new BaseScore();
                            baseScore.setSupplementCode(row.get(0) != null ? row.get(0).toString() : "");
                            baseScore.setTopic(row.get(1) != null ? row.get(1).toString() : "");
                            
                            // Парсим базовые баллы
                            if (row.get(2) != null) {
                                try {
                                    double score = Double.parseDouble(row.get(2).toString());
                                    baseScore.setBaseScore(score);
                                } catch (NumberFormatException e) {
                                    baseScore.setBaseScore(0.0);
                                }
                            }
                            
                            baseScore.setDescription(row.get(3) != null ? row.get(3).toString() : "");
                            
                            baseScores.add(baseScore);
                        } catch (Exception e) {
                            System.err.println("❌ Ошибка парсинга базового балла: " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("✅ Загружено базовых баллов: " + baseScores.size());
            return baseScores;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки базовых баллов: " + e.getMessage());
            throw e;
        }
    }

    // Внутренние классы для представления данных
    public static class Question {
        private String id;
        private String category;
        private String text;
        private String type;
        private String options;

        // Геттеры и сеттеры
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }
    }

    public static class AnswerScore {
        private String questionId;
        private String answer;
        private String supplementCode;
        private double score;
        private String description;

        // Геттеры и сеттеры
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        
        public String getSupplementCode() { return supplementCode; }
        public void setSupplementCode(String supplementCode) { this.supplementCode = supplementCode; }
        
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class BaseScore {
        private String supplementCode;
        private String topic;
        private double baseScore;
        private String description;

        // Геттеры и сеттеры
        public String getSupplementCode() { return supplementCode; }
        public void setSupplementCode(String supplementCode) { this.supplementCode = supplementCode; }
        
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public double getBaseScore() { return baseScore; }
        public void setBaseScore(double baseScore) { this.baseScore = baseScore; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

