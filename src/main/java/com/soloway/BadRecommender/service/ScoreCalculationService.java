package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Supplement;
import com.soloway.BadRecommender.model.SupplementScore;
import com.soloway.BadRecommender.model.UserAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreCalculationService {

    private final GoogleSheetsDataService googleSheetsDataService;
    private final GoogleSheetsService googleSheetsService;

    @Autowired
    public ScoreCalculationService(GoogleSheetsDataService googleSheetsDataService,
                                  GoogleSheetsService googleSheetsService) {
        this.googleSheetsDataService = googleSheetsDataService;
        this.googleSheetsService = googleSheetsService;
    }

    /**
     * Рассчитывает баллы для добавок на основе ответов пользователя
     */
    public RecommendationResult calculateScores(List<UserAnswer> userAnswers, String selectedCategory) throws IOException {
        System.out.println("🔍 Начинаем расчет баллов для категории: " + selectedCategory);
        
        // Загружаем все добавки
        List<Supplement> supplements = googleSheetsService.loadSupplements();
        System.out.println("💊 Загружено добавок: " + supplements.size());
        
        // Загружаем правила начисления баллов
        List<GoogleSheetsDataService.AnswerScore> answerScores = googleSheetsDataService.loadAnswerScores();
        System.out.println("📊 Загружено правил начисления баллов: " + answerScores.size());
        
        // Загружаем базовые баллы для выбранной темы
        List<GoogleSheetsDataService.BaseScore> baseScores = googleSheetsDataService.loadBaseScores();
        System.out.println("🏆 Загружено базовых баллов: " + baseScores.size());
        
        // Инициализируем баллы для всех добавок с базовыми баллами
        Map<String, SupplementScore> supplementScores = new HashMap<>();
        for (Supplement supplement : supplements) {
            // Ищем базовые баллы для этой добавки и темы
            double baseScore = baseScores.stream()
                .filter(bs -> bs.getSupplementCode().equals(supplement.getCode()) && 
                             bs.getTopic().equalsIgnoreCase(selectedCategory))
                .mapToDouble(GoogleSheetsDataService.BaseScore::getBaseScore)
                .sum();
            
            SupplementScore supplementScore = new SupplementScore(supplement.getName(), (int) baseScore);
            if (baseScore > 0) {
                System.out.println("🏆 " + supplement.getName() + " получил базовые " + baseScore + " баллов за тему '" + selectedCategory + "'");
            }
            supplementScores.put(supplement.getCode(), supplementScore);
        }
        
        // Проходим по всем ответам пользователя
        for (UserAnswer userAnswer : userAnswers) {
            System.out.println("🔍 Обрабатываем ответ на вопрос: " + userAnswer.getQuestionId() + " = " + userAnswer.getAnswer());
            
            // Ищем правила для этого вопроса и ответа
            List<GoogleSheetsDataService.AnswerScore> relevantRules = answerScores.stream()
                .filter(rule -> rule.getQuestionId().equals(userAnswer.getQuestionId()) &&
                               rule.getAnswer().equalsIgnoreCase(userAnswer.getAnswer()))
                .collect(Collectors.toList());
            
            // Применяем баллы
            for (GoogleSheetsDataService.AnswerScore rule : relevantRules) {
                String supplementCode = rule.getSupplementCode();
                double score = rule.getScore();
                
                if (supplementScores.containsKey(supplementCode)) {
                    SupplementScore supplementScore = supplementScores.get(supplementCode);
                    supplementScore.addScore((int) score);
                    System.out.println("📊 " + supplementScore.getSupplementName() + " получил " + score + " баллов за ответ '" + userAnswer.getAnswer() + "' на вопрос '" + userAnswer.getQuestionId() + "'");
                }
            }
        }
        
        // Сортируем добавки по баллам
        List<SupplementScore> sortedScores = supplementScores.values().stream()
            .filter(score -> score.getCurrentScore() > 0)
            .sorted(Comparator.comparing(SupplementScore::getCurrentScore).reversed())
            .collect(Collectors.toList());
        
        System.out.println("✅ Расчет баллов завершен. Найдено добавок с баллами: " + sortedScores.size());
        
        // Формируем рекомендации
        return generateRecommendations(sortedScores, supplements);
    }

    /**
     * Генерирует основные и дополнительные рекомендации
     */
    private RecommendationResult generateRecommendations(List<SupplementScore> sortedScores, List<Supplement> supplements) {
        List<SupplementWithScore> mainRecommendations = new ArrayList<>();
        List<SupplementWithScore> additionalRecommendations = new ArrayList<>();
        
        // Основные рекомендации (топ-3)
        for (int i = 0; i < Math.min(3, sortedScores.size()); i++) {
            SupplementScore supplementScore = sortedScores.get(i);
            // Находим полную информацию о добавке по имени
            Supplement supplement = supplements.stream()
                .filter(s -> s.getName().equals(supplementScore.getSupplementName()))
                .findFirst()
                .orElse(null);
            
            if (supplement != null) {
                mainRecommendations.add(new SupplementWithScore(supplement, supplementScore.getCurrentScore()));
            }
        }
        
        // Дополнительные рекомендации (следующие 2)
        for (int i = 3; i < Math.min(5, sortedScores.size()); i++) {
            SupplementScore supplementScore = sortedScores.get(i);
            // Находим полную информацию о добавке по имени
            Supplement supplement = supplements.stream()
                .filter(s -> s.getName().equals(supplementScore.getSupplementName()))
                .findFirst()
                .orElse(null);
            
            if (supplement != null) {
                additionalRecommendations.add(new SupplementWithScore(supplement, supplementScore.getCurrentScore()));
            }
        }
        
        System.out.println("🏆 Основных рекомендаций: " + mainRecommendations.size());
        System.out.println("📋 Дополнительных рекомендаций: " + additionalRecommendations.size());
        
        return new RecommendationResult(mainRecommendations, additionalRecommendations);
    }

    /**
     * Результат расчета рекомендаций с полной информацией о добавках
     */
    public static class RecommendationResult {
        private final List<SupplementWithScore> mainRecommendations;
        private final List<SupplementWithScore> additionalRecommendations;

        public RecommendationResult(List<SupplementWithScore> mainRecommendations, 
                                   List<SupplementWithScore> additionalRecommendations) {
            this.mainRecommendations = mainRecommendations;
            this.additionalRecommendations = additionalRecommendations;
        }

        public List<SupplementWithScore> getMainRecommendations() {
            return mainRecommendations;
        }

        public List<SupplementWithScore> getAdditionalRecommendations() {
            return additionalRecommendations;
        }
    }

    /**
     * Добавка с баллами
     */
    public static class SupplementWithScore {
        private final Supplement supplement;
        private final int score;

        public SupplementWithScore(Supplement supplement, int score) {
            this.supplement = supplement;
            this.score = score;
        }

        public Supplement getSupplement() {
            return supplement;
        }

        public int getScore() {
            return score;
        }
    }
}
