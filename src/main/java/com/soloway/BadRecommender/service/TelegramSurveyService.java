package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.TelegramUser;
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
        List<SurveyQuestion> topicQuestions = getQuestionsByTopic(selectedTopic);
        
        if (questionIndex >= topicQuestions.size()) {
            return null; // Опрос завершен
        }
        
        return topicQuestions.get(questionIndex);
    }

    /**
     * Получить вопросы по выбранной теме
     */
    private List<SurveyQuestion> getQuestionsByTopic(String topic) {
        switch (topic) {
            case "iron":
                return createIronQuestions();
            case "energy":
                return createEnergyQuestions();
            case "sleep":
                return createSleepQuestions();
            case "weight":
                return createWeightQuestions();
            case "skin":
                return createSkinQuestions();
            case "digestion":
                return createDigestionQuestions();
            default:
                return createGeneralQuestions();
        }
    }

    // Вопросы для темы "Поднять гемоглобин"
    private List<SurveyQuestion> createIronQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();
        
        questions.add(SurveyQuestion.builder()
            .text("Сообщал ли вам врач о низких запасах железа или снижении гемоглобина?")
            .options(Arrays.asList("нет", "да", "не знаю"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Бывает ли у вас слабость, бледность, утомляемость при подъёме по лестнице?")
            .options(Arrays.asList("нет", "немного", "сильно"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Возникают ли головокружения при вставании или одышка при лёгкой нагрузке?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Были ли недавно донорства или кровопотери (у женщин — обильные менструации)?")
            .options(Arrays.asList("нет", "было", "да, регулярно"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Придерживаетесь ли вегетарианского/веганского питания?")
            .options(Arrays.asList("нет", "да, частично", "да, строго"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        return questions;
    }

    // Вопросы для темы "Бодрость и энергия"
    private List<SurveyQuestion> createEnergyQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();
        
        questions.add(SurveyQuestion.builder()
            .text("Часто ли вас клонит в сон после обеда?")
            .options(Arrays.asList("редко", "иногда", "почти каждый день"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Сколько чашек кофе вы выпиваете в день?")
            .options(Arrays.asList("0-1", "2-3", "4+"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Как вы чувствуете себя утром после пробуждения?")
            .options(Arrays.asList("бодро", "нормально", "тяжело встаю"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Бывает ли упадок сил во второй половине дня?")
            .options(Arrays.asList("нет", "иногда", "почти всегда"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Как часто вы чувствуете усталость без видимых причин?")
            .options(Arrays.asList("редко", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        return questions;
    }

    // Вопросы для темы "Крепкий сон, меньше стресса"
    private List<SurveyQuestion> createSleepQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();
        
        questions.add(SurveyQuestion.builder()
            .text("Как вы оцениваете качество своего сна?")
            .options(Arrays.asList("хорошее", "удовлетворительное", "плохое"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Как часто вы испытываете стресс?")
            .options(Arrays.asList("редко", "иногда", "постоянно"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Сколько времени вам нужно, чтобы заснуть?")
            .options(Arrays.asList("менее 15 минут", "15-30 минут", "более 30 минут"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Часто ли вы просыпаетесь ночью?")
            .options(Arrays.asList("редко", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Как вы чувствуете себя утром после сна?")
            .options(Arrays.asList("отдохнувшим", "нормально", "уставшим"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        return questions;
    }

    // Вопросы для темы "Контроль веса и аппетита"
    private List<SurveyQuestion> createWeightQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();
        
        questions.add(SurveyQuestion.builder()
            .text("Сложно ли вам остановиться на одной порции, бывает переедание?")
            .options(Arrays.asList("нет", "иногда", "почти всегда"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Случаются ли ночные перекусы или \"заедание\" стресса?")
            .options(Arrays.asList("нет", "иногда", "почти всегда"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Часто ли ужин приходится менее чем за 2 часа до сна?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Бывает ли, что вы пропускаете завтрак 3 и более раз в неделю?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Бывает ли сочетание: вздутие/тяжесть и выраженная тяга к сладкому?")
            .options(Arrays.asList("нет", "иногда", "почти всегда"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        return questions;
    }

    // Вопросы для темы "Чистая кожа, крепкие волосы"
    private List<SurveyQuestion> createSkinQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();
        
        questions.add(SurveyQuestion.builder()
            .text("Есть ли проблемы с кожей (акне, воспаления, сухость)?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Выпадают ли волосы больше обычного?")
            .options(Arrays.asList("нет", "немного", "сильно"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Есть ли проблемы с ногтями (ломкость, слоение)?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Как часто вы подвергаетесь стрессу?")
            .options(Arrays.asList("редко", "иногда", "постоянно"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Есть ли проблемы с пищеварением?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        return questions;
    }

    // Вопросы для темы "Комфорт пищеварения"
    private List<SurveyQuestion> createDigestionQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();
        
        questions.add(SurveyQuestion.builder()
            .text("Принимали ли вы антибиотики за последние 6 месяцев?")
            .options(Arrays.asList("нет", "да", "не помню"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Замечаете ли, что молочные продукты ухудшают самочувствие (возникает вздутие)?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Возникают ли тяжесть или дискомфорт после еды?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Сообщал ли врач о синдроме раздражённого кишечника или возможном воспалении?")
            .options(Arrays.asList("нет", "да", "не знаю"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Усиливаются ли ЖКТ-симптомы на фоне стресса?")
            .options(Arrays.asList("нет", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        return questions;
    }

    // Общие вопросы для всех тем
    private List<SurveyQuestion> createGeneralQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();
        
        questions.add(SurveyQuestion.builder()
            .text("Как часто вы употребляете рыбу/морепродукты?")
            .options(Arrays.asList("почти никогда", "реже 1 раза в неделю", "1–2 раза в неделю", "3+ раз в неделю"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Сколько чашек кофе вы выпиваете в день?")
            .options(Arrays.asList("0", "1", "2–3", "4+"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Какой у вас уровень физической активности?")
            .options(Arrays.asList("нет регулярных тренировок", "1–2 тренировки в неделю", "3–4", "5+"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Курите ли вы сейчас или используете вейп?")
            .options(Arrays.asList("нет", "иногда", "регулярно"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        questions.add(SurveyQuestion.builder()
            .text("Часто ли вас тянет на сладкое/рафинированные углеводы?")
            .options(Arrays.asList("нет", "редко", "иногда", "часто"))
            .questionType(QuestionType.DYNAMIC)
            .build());
            
        return questions;
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
        
        List<SurveyQuestion> topicQuestions = getQuestionsByTopic(selectedTopic);
        int totalQuestions = 1 + topicQuestions.size(); // 1 для выбора темы + вопросы по теме
        
        return user.getCurrentQuestionIndex() >= totalQuestions;
    }

    /**
     * Получить общее количество вопросов для темы
     */
    public int getTotalQuestionsForTopic(String topic) {
        List<SurveyQuestion> questions = getQuestionsByTopic(topic);
        return 1 + questions.size(); // 1 для выбора темы + вопросы по теме
    }

    /**
     * Класс для представления вопроса опроса
     */
    public static class SurveyQuestion {
        private String text;
        private List<String> options;
        private QuestionType questionType;

        private SurveyQuestion(Builder builder) {
            this.text = builder.text;
            this.options = builder.options;
            this.questionType = builder.questionType;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Геттеры
        public String getText() { return text; }
        public List<String> getOptions() { return options; }
        public QuestionType getQuestionType() { return questionType; }

        public static class Builder {
            private String text;
            private List<String> options;
            private QuestionType questionType;

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
