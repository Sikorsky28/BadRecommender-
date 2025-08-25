package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.UserAnswer;
import com.soloway.BadRecommender.model.Supplement;
import com.soloway.BadRecommender.model.SupplementScore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationCalculationService {

    private final GoogleSheetsService googleSheetsService;
    private final FallbackDataService fallbackDataService;

    public RecommendationCalculationService(GoogleSheetsService googleSheetsService, FallbackDataService fallbackDataService) {
        this.googleSheetsService = googleSheetsService;
        this.fallbackDataService = fallbackDataService;
        System.out.println("✅ RecommendationCalculationService инициализирован с GoogleSheetsService");
    }

    // Конструктор для случая, когда GoogleSheetsService недоступен
    public RecommendationCalculationService(FallbackDataService fallbackDataService) {
        this.googleSheetsService = null;
        this.fallbackDataService = fallbackDataService;
        System.out.println("⚠️ RecommendationCalculationService инициализирован без GoogleSheetsService");
    }

    public RecommendationResult calculateRecommendations(List<UserAnswer> answers, String selectedTopic) {
        System.out.println("🧮 Начинаем расчет рекомендаций...");
        System.out.println("   Ответов: " + answers.size());
        System.out.println("   Выбранная тема: " + selectedTopic);

        // Получаем все добавки
        List<Supplement> allSupplements;
        if (googleSheetsService == null) {
            System.out.println("⚠️ GoogleSheetsService недоступен, используем fallback данные");
            allSupplements = fallbackDataService.getFallbackSupplements();
        } else {
            try {
                allSupplements = googleSheetsService.loadSupplements();
            } catch (Exception e) {
                System.out.println("⚠️ Ошибка загрузки из Google Sheets: " + e.getMessage());
                System.out.println("⚠️ Используем fallback данные");
                allSupplements = fallbackDataService.getFallbackSupplements();
            }
            
            // Если добавки не загружены, используем fallback данные
            if (allSupplements.isEmpty()) {
                System.out.println("⚠️ Добавки не загружены, используем fallback данные");
                allSupplements = fallbackDataService.getFallbackSupplements();
            }
        }
        
        // Рассчитываем баллы для каждой добавки
        Map<Supplement, Double> supplementScores = new HashMap<>();
        
        for (Supplement supplement : allSupplements) {
            double score = calculateSupplementScore(supplement, answers, selectedTopic);
            supplementScores.put(supplement, score);
        }

        // Сортируем добавки по баллам
        List<Supplement> sortedSupplements = supplementScores.entrySet().stream()
                .sorted(Map.Entry.<Supplement, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Выбираем топ-3 основные рекомендации
        List<Supplement> mainRecommendations = sortedSupplements.stream()
                .limit(3)
                .collect(Collectors.toList());

        // Выбираем 2 дополнительные рекомендации (4-5 места)
        List<Supplement> additionalRecommendations = sortedSupplements.stream()
                .skip(3)
                .limit(2)
                .collect(Collectors.toList());

        // Собираем детали по каждой добавке
        List<SupplementScore> supplementDetails = sortedSupplements.stream()
                .limit(5)
                .map(supplement -> {
                    SupplementScore score = new SupplementScore(
                            supplement.getName(),
                            supplementScores.get(supplement).intValue()
                    );
                    return score;
                })
                .collect(Collectors.toList());

        System.out.println("   Основных рекомендаций: " + mainRecommendations.size());
        System.out.println("   Дополнительных рекомендаций: " + additionalRecommendations.size());
        
        // Логируем основные рекомендации с описаниями
        System.out.println("🏆 Основные рекомендации:");
        for (Supplement supplement : mainRecommendations) {
            System.out.println("   - " + supplement.getName() + ": " + supplement.getDescription());
        }

        return new RecommendationResult(mainRecommendations, additionalRecommendations, supplementDetails);
    }

    private double calculateSupplementScore(Supplement supplement, List<UserAnswer> answers, String selectedTopic) {
        double score = 0.0;

        // 1. Стартовые баллы по выбранной теме (согласно ТЗ)
        score += calculateInitialScores(supplement, selectedTopic);

        // 2. Баллы за ответы на уточняющие вопросы
        score += calculateAnswerScores(supplement, answers);

        // 3. Баллы за общие факторы (кофе, тренировки, курение)
        score += calculateGeneralFactorScores(supplement, answers);

        return score;
    }

    private double calculateInitialScores(Supplement supplement, String selectedTopic) {
        double score = 0.0;
        
        // Проверяем соответствие тегов добавки выбранной теме
        if (supplement.getTags().contains(selectedTopic)) {
                score += 3; // Базовый балл за соответствие теме
                
                // Дополнительные баллы за точное соответствие
            switch (selectedTopic) {
                    case "energy":
                        if (supplement.getName().contains("Energy")) score += 2;
                        if (supplement.getName().contains("Coenzyme Q10")) score += 1;
                        if (supplement.getName().contains("Iron bisglycinate")) score += 1;
                        if (supplement.getName().contains("Tyrosine")) score += 1;
                        break;
                    case "sleep":
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                        if (supplement.getName().contains("5-HTP")) score += 2;
                        if (supplement.getName().contains("SAMe")) score += 1;
                        break;
                case "weight":
                    if (supplement.getName().contains("Appetite Control")) score += 3;
                    if (supplement.getName().contains("Active Slim")) score += 2;
                    if (supplement.getName().contains("Comfort Slim")) score += 2;
                    if (supplement.getName().contains("Alpha-lipoic acid")) score += 2;
                    break;
                case "skin":
                    if (supplement.getName().contains("Collagen")) score += 3;
                    if (supplement.getName().contains("Glutathione")) score += 3;
                    if (supplement.getName().contains("Hyaluronic acid")) score += 2;
                    if (supplement.getName().contains("Hair Complex")) score += 2;
                    if (supplement.getName().contains("Biotin")) score += 1;
                    break;
                case "digestion":
                    if (supplement.getName().contains("Prebio Complex")) score += 3;
                    if (supplement.getName().contains("Synbiotic")) score += 2;
                    if (supplement.getName().contains("Curcumin")) score += 2;
                    if (supplement.getName().contains("SAMe")) score += 1;
                    break;
                case "joints":
                    if (supplement.getName().contains("Glucosamine")) score += 3;
                    if (supplement.getName().contains("Collagen")) score += 2;
                    if (supplement.getName().contains("Hyaluronic acid")) score += 2;
                    if (supplement.getName().contains("Vitamin D3")) score += 2;
                    break;
                case "immunity":
                    if (supplement.getName().contains("Zinc")) score += 2;
                    if (supplement.getName().contains("Selenium")) score += 2;
                    if (supplement.getName().contains("Lactoferrin")) score += 2;
                    if (supplement.getName().contains("Vitamin C")) score += 2;
                    if (supplement.getName().contains("Gingko Biloba")) score += 2;
                    if (supplement.getName().contains("Vitamin D3")) score += 2;
                    break;
                case "heart":
                    if (supplement.getName().contains("Omega-3")) score += 3;
                    if (supplement.getName().contains("Coenzyme Q10")) score += 2;
                    if (supplement.getName().contains("Resveratrol")) score += 2;
                    if (supplement.getName().contains("TMG")) score += 2;
                    if (supplement.getName().contains("Curcumin")) score += 2;
                    break;
                case "thyroid":
                    if (supplement.getName().contains("Tyrosine")) score += 3;
                    if (supplement.getName().contains("Selenium")) score += 1;
                    break;
                case "cycle":
                    if (supplement.getName().contains("Inositol")) score += 3;
                    if (supplement.getName().contains("DIM")) score += 2;
                    if (supplement.getName().contains("Zinc")) score += 1;
                    break;
                case "menopause":
                    if (supplement.getName().contains("Lignagallat")) score += 3;
                    if (supplement.getName().contains("DIM")) score += 1;
                    if (supplement.getName().contains("Omega-3")) score += 1;
                    break;
                case "prostate":
                    if (supplement.getName().contains("nO-Prost")) score += 3;
                    if (supplement.getName().contains("Men's Formula")) score += 2;
                    if (supplement.getName().contains("Zinc")) score += 1;
                    break;
                case "iron":
                    if (supplement.getName().contains("Iron bisglycinate")) score += 3;
                    if (supplement.getName().contains("Iron Chelate")) score += 2;
                    if (supplement.getName().contains("Lactoferrin")) score += 3;
                    break;
            }
        }
        
        return score;
    }

    private double calculateAnswerScores(Supplement supplement, List<UserAnswer> answers) {
        double score = 0.0;
        
        for (UserAnswer answer : answers) {
            // Уточняющие вопросы согласно ТЗ
            switch (answer.getQuestionId()) {
                case "afternoon_sleep":
                    if ("sometimes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Energy")) score += 1;
                        if (supplement.getName().contains("CoQ10")) score += 1;
                    } else if ("almost_everyday".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Energy")) score += 2;
                        if (supplement.getName().contains("CoQ10")) score += 2;
                    }
                    break;
                case "coffee_amount":
                    if ("2-3".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 1;
                    } else if ("4+".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    }
                    break;
                case "sleep_time":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("5-HTP")) score += 3;
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    }
                    break;
                case "anxiety_level":
                    if ("7-8".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("SAMe")) score += 1;
                        if (supplement.getName().contains("Magnesium B6")) score += 1;
                    } else if ("9-10".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("SAMe")) score += 2;
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    }
                    break;
                case "weight_difficulty":
                    if ("hunger".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Appetite Control")) score += 3;
                    } else if ("sweet_craving".equals(answer.getAnswer())) {
                        // Berberine удален из системы
                    }
                    break;
                case "dry_skin":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Hyaluronic acid")) score += 3;
                        if (supplement.getName().contains("Collagen")) score += 2;
                    }
                    break;

                case "bloating":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Prebio Complex")) score += 3;
                    }
                    break;

                case "frequent_colds":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 2;
                        if (supplement.getName().contains("Vitamin D3")) score += 1;
                    }
                    break;
                case "ldl_level":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 3;
                    }
                    break;
                case "tsh_level":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 3;
                        if (supplement.getName().contains("Selenium")) score += 1;
                    }
                    break;
                case "cycle_length":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Inositol")) score += 3;
                    }
                    break;
                case "hot_flashes":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lignagallat")) score += 3;
                    }
                    break;
                case "urine_stream":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("nO-Prost")) score += 3;
                    }
                    break;
                case "ferritin_level":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron bisglycinate")) score += 3;
                    }
                    break;
                case "iron_tolerance":
                    if ("yes".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lactoferrin")) score += 3;
                        // Iron bisglycinate → 0 (противопоказание)
                        if (supplement.getName().contains("Iron bisglycinate")) score = 0;
                    }
                    break;
                    
                // Новые вопросы для темы "железо"
                case "iron_deficiency_doctor":
                    if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 3;
                    } else if ("не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 1;
                    }
                    break;
                    
                case "weakness_fatigue":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 2;
                    } else if ("сильно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 3;
                    }
                    break;
                    
                case "dizziness_shortness":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 3;
                    }
                    break;
                    
                case "blood_loss":
                    if ("было".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 2;
                    } else if ("да, регулярно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 3;
                    }
                    break;
                    
                case "vegetarian_vegan":
                    if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 3;
                        if (supplement.getName().contains("Lactoferrin")) score += 2;
                    }
                    break;
                    
                case "gut_sensitivity":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lactoferrin")) score += 2;
                    } else if ("выраженно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lactoferrin")) score += 3;
                    }
                    break;
                    
                // Вопросы для темы "Бодрость и энергия"
                case "morning_energy":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 2;
                        if (supplement.getName().contains("Energy")) score += 2;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 3;
                        if (supplement.getName().contains("Energy")) score += 3;
                    }
                    break;
                    
                case "afternoon_crash":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Energy")) score += 2;
                        if (supplement.getName().contains("CoQ10")) score += 1;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Energy")) score += 3;
                        if (supplement.getName().contains("CoQ10")) score += 2;
                    }
                    break;
                    
                case "post_infection_fatigue":
                    if ("было, но прошло".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 2;
                        if (supplement.getName().contains("Energy")) score += 1;
                    } else if ("да, держится".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 3;
                        if (supplement.getName().contains("Energy")) score += 3;
                    }
                    break;
                    
                case "exercise_fatigue":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 2;
                        if (supplement.getName().contains("Energy")) score += 2;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 3;
                        if (supplement.getName().contains("Energy")) score += 3;
                    }
                    break;
                    
                case "iron_anemia_doctor":
                    if ("не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 2;
                        if (supplement.getName().contains("CoQ10")) score += 1;
                    } else if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Iron Chelate")) score += 3;
                        if (supplement.getName().contains("CoQ10")) score += 2;
                    }
                    break;
                    
                case "caffeine_sensitivity":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 3;
                    }
                    break;
                    
                // Вопросы для темы "Крепкий сон, меньше стресса"
                case "sleep_onset":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("5-HTP")) score += 2;
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("5-HTP")) score += 3;
                        if (supplement.getName().contains("Magnesium B6")) score += 3;
                    }
                    break;
                    
                case "night_awakenings":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                        if (supplement.getName().contains("5-HTP")) score += 2;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 3;
                        if (supplement.getName().contains("5-HTP")) score += 3;
                    }
                    break;
                    
                case "chronic_stress":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Complex B-SAMe")) score += 2;
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Complex B-SAMe")) score += 3;
                        if (supplement.getName().contains("Magnesium B6")) score += 3;
                    }
                    break;
                    
                case "low_motivation":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Complex B-SAMe")) score += 2;
                        if (supplement.getName().contains("5-HTP")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Complex B-SAMe")) score += 3;
                        if (supplement.getName().contains("5-HTP")) score += 3;
                    }
                    break;
                    
                case "screen_before_bed":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 1;
                        if (supplement.getName().contains("5-HTP")) score += 1;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                        if (supplement.getName().contains("5-HTP")) score += 2;
                    }
                    break;
                    
                case "stress_impact":
                    if ("скорее да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                        if (supplement.getName().contains("Complex B-SAMe")) score += 2;
                        if (supplement.getName().contains("5-HTP")) score += 1;
                    } else if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 3;
                        if (supplement.getName().contains("Complex B-SAMe")) score += 3;
                        if (supplement.getName().contains("5-HTP")) score += 2;
                    }
                    break;
                    
                // Вопросы для темы "Контроль веса и аппетита"
                case "portion_control":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Appetite Control")) score += 2;
                        if (supplement.getName().contains("Active Slim")) score += 1;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Appetite Control")) score += 3;
                        if (supplement.getName().contains("Active Slim")) score += 2;
                    }
                    break;
                    
                case "night_snacking":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Comfort Slim")) score += 2;
                        if (supplement.getName().contains("Appetite Control")) score += 1;
                    } else if ("почти всегда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Comfort Slim")) score += 3;
                        if (supplement.getName().contains("Appetite Control")) score += 2;
                    }
                    break;
                    
                case "late_dinner":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Comfort Slim")) score += 1;
                        if (supplement.getName().contains("Active Slim")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Comfort Slim")) score += 2;
                        if (supplement.getName().contains("Active Slim")) score += 2;
                    }
                    break;
                    
                case "skip_breakfast":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Alpha-lipoic")) score += 1;
                        if (supplement.getName().contains("Appetite Control")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Alpha-lipoic")) score += 2;
                        if (supplement.getName().contains("Appetite Control")) score += 2;
                    }
                    break;
                    
                case "bloating_sugar_cravings":
                    // Berberine удален из системы
                    break;
                    
                case "waist_increase":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Alpha-lipoic")) score += 2;
                        // Berberine удален из системы
                    } else if ("выраженно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Alpha-lipoic")) score += 3;
                        // Berberine удален из системы
                    }
                    break;
                    
                // Вопросы для темы "Чистая кожа, крепкие волосы"
                case "deep_acne":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 2;
                        if (supplement.getName().contains("Glutathione")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 3;
                        if (supplement.getName().contains("Glutathione")) score += 2;
                    }
                    break;
                    
                case "acne_marks":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Glutathione")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Glutathione")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                case "wrinkles_elasticity":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Collagen")) score += 2;
                        if (supplement.getName().contains("Hyaluronic")) score += 1;
                    } else if ("выраженно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Collagen")) score += 3;
                        if (supplement.getName().contains("Hyaluronic")) score += 2;
                    }
                    break;
                    
                case "hair_loss":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Hair Complex")) score += 2;
                        if (supplement.getName().contains("Biotin")) score += 1;
                    } else if ("заметно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Hair Complex")) score += 3;
                        if (supplement.getName().contains("Biotin")) score += 2;
                    }
                    break;
                    
                case "scalp_issues":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Hair Complex")) score += 2;
                        if (supplement.getName().contains("Zinc")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Hair Complex")) score += 3;
                        if (supplement.getName().contains("Zinc")) score += 2;
                    }
                    break;
                    
                case "skin_irritation":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 2;
                        if (supplement.getName().contains("Glutathione")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 3;
                        if (supplement.getName().contains("Glutathione")) score += 2;
                    }
                    break;
                    
                // Вопросы для темы "Комфорт пищеварения"
                case "recent_antibiotics":
                    if ("да".equals(answer.getAnswer()) || "не помню".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Synbiotic")) score += 3;
                    }
                    break;
                    
                case "dairy_intolerance":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Prebio")) score += 2;
                        if (supplement.getName().contains("Synbiotic")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Prebio")) score += 3;
                        if (supplement.getName().contains("Synbiotic")) score += 2;
                    }
                    break;
                    
                case "post_meal_discomfort":
                    if ("иногда".equals(answer.getAnswer())) {
                        // Berberine удален из системы
                        if (supplement.getName().contains("Curcumin")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        // Berberine удален из системы
                        if (supplement.getName().contains("Curcumin")) score += 2;
                    }
                    break;
                    
                case "ibs_diagnosis":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Curcumin")) score += 3;
                        if (supplement.getName().contains("Synbiotic")) score += 2;
                    }
                    break;
                    
                case "stress_gut_symptoms":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Complex B-SAMe")) score += 2;
                        if (supplement.getName().contains("Prebio")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Complex B-SAMe")) score += 3;
                        if (supplement.getName().contains("Prebio")) score += 2;
                    }
                    break;
                    
                case "bloating_sweet_cravings":
                    if ("иногда".equals(answer.getAnswer())) {
                        // Berberine удален из системы
                        if (supplement.getName().contains("Synbiotic")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        // Berberine удален из системы
                        if (supplement.getName().contains("Synbiotic")) score += 2;
                    }
                    break;
                    
                // Вопросы для темы "Подвижные суставы, крепкие кости"
                case "regular_exercise":
                    if ("1-2 раза в неделю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Collagen")) score += 1;
                        if (supplement.getName().contains("Glucosamine")) score += 1;
                    } else if ("3 и более раз в неделю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Collagen")) score += 2;
                        if (supplement.getName().contains("Glucosamine")) score += 2;
                    }
                    break;
                    
                case "recent_injuries":
                    if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Glucosamine")) score += 2;
                        if (supplement.getName().contains("Collagen")) score += 2;
                    } else if ("повторные".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Glucosamine")) score += 3;
                        if (supplement.getName().contains("Collagen")) score += 3;
                    }
                    break;
                    
                case "joint_dryness":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Hyaluronic")) score += 2;
                        if (supplement.getName().contains("Collagen")) score += 1;
                    } else if ("выраженно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Hyaluronic")) score += 3;
                        if (supplement.getName().contains("Collagen")) score += 2;
                    }
                    break;
                    
                case "age_category":
                    if ("35–49 лет".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Collagen")) score += 2;
                        if (supplement.getName().contains("Glucosamine")) score += 1;
                    } else if ("50+ лет".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Collagen")) score += 3;
                        if (supplement.getName().contains("Glucosamine")) score += 2;
                    }
                    break;
                    
                case "knee_pain":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Glucosamine")) score += 2;
                        if (supplement.getName().contains("Curcumin")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Glucosamine")) score += 3;
                        if (supplement.getName().contains("Curcumin")) score += 2;
                    }
                    break;
                    
                case "bone_density_risk":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Vitamin D3")) score += 3;
                    }
                    break;
                    
                // Вопросы для темы "Сильный иммунитет"
                case "social_contact":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lactoferrin")) score += 1;
                        if (supplement.getName().contains("Vitamin C")) score += 1;
                        if (supplement.getName().contains("Zinc")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lactoferrin")) score += 2;
                        if (supplement.getName().contains("Vitamin C")) score += 2;
                        if (supplement.getName().contains("Zinc")) score += 2;
                    }
                    break;
                    
                case "prolonged_colds":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Vitamin C")) score += 2;
                        if (supplement.getName().contains("Zinc")) score += 1;
                        if (supplement.getName().contains("Lactoferrin")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Vitamin C")) score += 3;
                        if (supplement.getName().contains("Zinc")) score += 2;
                        if (supplement.getName().contains("Lactoferrin")) score += 2;
                    }
                    break;
                    
                case "frequent_infections":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lactoferrin")) score += 2;
                        if (supplement.getName().contains("Vitamin C")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lactoferrin")) score += 3;
                        if (supplement.getName().contains("Vitamin C")) score += 3;
                    }
                    break;
                    
                case "cold_extremities":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Ginkgo Biloba")) score += 2;
                        if (supplement.getName().contains("Vitamin C")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Ginkgo Biloba")) score += 3;
                        if (supplement.getName().contains("Vitamin C")) score += 2;
                    }
                    break;
                    
                case "autoimmune_thyroid":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Selenium")) score += 3;
                        if (supplement.getName().contains("Vitamin D3")) score += 2;
                    }
                    break;
                    
                case "sleep_deprivation":
                    if ("1-2 раза в неделю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Vitamin C")) score += 1;
                        if (supplement.getName().contains("Zinc")) score += 1;
                    } else if ("3-4 раза в неделю или чаще".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Vitamin C")) score += 2;
                        if (supplement.getName().contains("Zinc")) score += 2;
                    }
                    break;
                    
                // Вопросы для темы "Здоровое сердце и сосуды"
                case "family_cardiovascular":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 3;
                        if (supplement.getName().contains("Resveratrol")) score += 2;
                        if (supplement.getName().contains("B-TMG")) score += 2;
                    }
                    break;
                    
                case "exercise_fatigue_heart":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                case "homocysteine_elevated":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("B-TMG")) score += 3;
                    }
                    break;
                    
                case "inflammation_joints":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Curcumin")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                        if (supplement.getName().contains("Resveratrol")) score += 2;
                    }
                    break;
                    
                case "sedentary_lifestyle":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 1;
                        if (supplement.getName().contains("Resveratrol")) score += 1;
                    } else if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 2;
                        if (supplement.getName().contains("Resveratrol")) score += 2;
                    }
                    break;
                    
                case "circulation_issues":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Resveratrol")) score += 2;
                        if (supplement.getName().contains("CoQ10")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Resveratrol")) score += 3;
                        if (supplement.getName().contains("CoQ10")) score += 2;
                    }
                    break;
                    
                // Вопросы для темы "Поддержка щитовидной железы"
                case "cold_sensitivity":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 3;
                    }
                    break;
                    
                case "tsh_elevated":
                    if ("не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 2;
                        if (supplement.getName().contains("Selenium")) score += 1;
                    } else if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 3;
                        if (supplement.getName().contains("Selenium")) score += 2;
                    }
                    break;
                    
                case "autoimmune_thyroid_specific":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Selenium")) score += 3;
                    }
                    break;
                    
                case "constipation":
                    if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Selenium")) score += 2;
                        if (supplement.getName().contains("Tyrosine")) score += 2;
                    }
                    break;
                    
                case "voice_neck_discomfort":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Selenium")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Selenium")) score += 3;
                    }
                    break;
                    
                case "evening_energy_drop":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Tyrosine")) score += 3;
                    }
                    break;
                    
                // Вопросы для темы "Регулярный цикл, мягкий ПМС"
                case "irregular_cycle":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Inositol")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Inositol")) score += 3;
                    }
                    break;
                    
                case "pms_symptoms":
                    if ("умеренно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 2;
                    } else if ("сильно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 3;
                    }
                    break;
                    
                case "premenstrual_acne":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 2;
                        if (supplement.getName().contains("DIM")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 3;
                        if (supplement.getName().contains("DIM")) score += 2;
                    }
                    break;
                    
                case "menstrual_pain":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 2;
                        if (supplement.getName().contains("DIM")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 3;
                        if (supplement.getName().contains("DIM")) score += 2;
                    }
                    break;
                    
                case "heavy_periods":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 2;
                        if (supplement.getName().contains("Zinc")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 3;
                        if (supplement.getName().contains("Zinc")) score += 2;
                    }
                    break;
                    
                case "pregnancy_planning":
                    if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Inositol")) score += 3;
                    }
                    break;
                    
                // Вопросы для темы "Менопауза без приливов"
                case "hot_flashes_frequent":
                    if ("да".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lignagallat")) score += 3;
                        if (supplement.getName().contains("DIM")) score += 2;
                    }
                    break;
                    
                case "night_hot_flashes":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lignagallat")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Lignagallat")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                case "mood_changes_menopause":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("сильно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                case "joint_pain_menopause":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 2;
                        if (supplement.getName().contains("Curcumin")) score += 1;
                    } else if ("выраженно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 3;
                        if (supplement.getName().contains("Curcumin")) score += 2;
                    }
                    break;
                    
                case "weight_gain_menopause":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("заметно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                case "family_hormone_cancer":
                    if ("да".equals(answer.getAnswer()) || "не знаю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("DIM")) score += 3;
                    }
                    break;
                    
                // Вопросы для темы "Мужское здоровье"
                case "urinary_symptoms":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("nO-Prost Complex")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("nO-Prost Complex")) score += 3;
                    }
                    break;
                    
                case "energy_libido_decline":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Men's Formula")) score += 2;
                        if (supplement.getName().contains("Zinc")) score += 1;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("сильно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Men's Formula")) score += 3;
                        if (supplement.getName().contains("Zinc")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                case "prostate_discomfort":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("nO-Prost Complex")) score += 2;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("nO-Prost Complex")) score += 3;
                    }
                    break;
                    
                case "male_hair_loss":
                    if ("немного".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 2;
                    } else if ("выраженно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 3;
                    }
                    break;
                    
                case "recovery_time":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Men's Formula")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Men's Formula")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                case "red_meat_consumption":
                    if ("1-4 раза в неделю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("каждый день".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 2;
                    }
                    break;
                    
                // Общие вопросы для всех тем
                case "fish_consumption":
                    if ("почти никогда".equals(answer.getAnswer()) || "реже 1 раза в неделю".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 3;
                    }
                    break;
                    
                case "coffee_daily":
                    if ("2–3".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    } else if ("4+".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 3;
                    }
                    break;
                    
                case "physical_activity":
                    if ("3–4".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 2;
                        if (supplement.getName().contains("Magnesium B6")) score += 1;
                    } else if ("5+".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("CoQ10")) score += 3;
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    }
                    break;
                    
                case "smoking_vaping":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Vitamin C")) score += 2;
                        if (supplement.getName().contains("Omega-3")) score += 1;
                    } else if ("регулярно".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Vitamin C")) score += 3;
                        if (supplement.getName().contains("Omega-3")) score += 2;
                        if (supplement.getName().contains("Curcumin")) score += 2;
                    }
                    break;
                    
                case "sweet_cravings":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Alpha-lipoic")) score += 2;
                        // Berberine удален из системы
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Alpha-lipoic")) score += 3;
                        // Berberine удален из системы
                    }
                    break;
                    
                case "digestive_issues":
                    if ("иногда".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Prebio")) score += 2;
                        if (supplement.getName().contains("Synbiotic")) score += 1;
                    } else if ("часто".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Prebio")) score += 3;
                        if (supplement.getName().contains("Synbiotic")) score += 2;
                    }
                    break;
                    
                case "cold_frequency":
                    if ("2–3".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 2;
                        if (supplement.getName().contains("Vitamin C")) score += 1;
                    } else if ("4+".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Zinc")) score += 3;
                        if (supplement.getName().contains("Vitamin C")) score += 2;
                        if (supplement.getName().contains("Lactoferrin")) score += 2;
                    }
                    break;
            }
        }
        
        return score;
    }

    private double calculateGeneralFactorScores(Supplement supplement, List<UserAnswer> answers) {
        double score = 0.0;
        
        for (UserAnswer answer : answers) {
            switch (answer.getQuestionId()) {
                case "coffee":
                    if ("2-3".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 1;
                    } else if ("4+".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 2;
                    }
                    break;
                case "training":
                    if ("4+".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Magnesium B6")) score += 1;
                        if (supplement.getName().contains("CoQ10")) score += 1;
                    }
                    break;
                case "smoking":
                    if ("5-9".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 1;
                        if (supplement.getName().contains("CoQ10")) score += 1;
                        if (supplement.getName().contains("Selenium")) score += 1;
                    } else if ("10+".equals(answer.getAnswer())) {
                        if (supplement.getName().contains("Omega-3")) score += 2;
                        if (supplement.getName().contains("CoQ10")) score += 2;
                        if (supplement.getName().contains("Selenium")) score += 2;
                    }
                    break;
            }
        }
        
        return score;
    }

    public static class RecommendationResult {
        private List<Supplement> mainRecommendations;
        private List<Supplement> additionalRecommendations;
        private List<SupplementScore> supplementDetails;

        public RecommendationResult(List<Supplement> mainRecommendations, 
                                  List<Supplement> additionalRecommendations,
                                  List<SupplementScore> supplementDetails) {
            this.mainRecommendations = mainRecommendations;
            this.additionalRecommendations = additionalRecommendations;
            this.supplementDetails = supplementDetails;
        }

        // Геттеры
        public List<Supplement> getMainRecommendations() { return mainRecommendations; }
        public List<Supplement> getAdditionalRecommendations() { return additionalRecommendations; }
        public List<SupplementScore> getSupplementDetails() { return supplementDetails; }

        // Сеттеры
        public void setMainRecommendations(List<Supplement> mainRecommendations) { 
            this.mainRecommendations = mainRecommendations; 
        }
        public void setAdditionalRecommendations(List<Supplement> additionalRecommendations) { 
            this.additionalRecommendations = additionalRecommendations; 
        }
        public void setSupplementDetails(List<SupplementScore> supplementDetails) { 
            this.supplementDetails = supplementDetails; 
        }
    }
}
