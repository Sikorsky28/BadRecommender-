package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Question;
import com.soloway.BadRecommender.model.UserAnswer;

import com.soloway.BadRecommender.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class RecommendationService {

  private final QuestionRepository questionRepository;
  private final RecommendationCalculationService calculationService;

  public RecommendationService(QuestionRepository questionRepository, 
                             RecommendationCalculationService calculationService) {
    this.questionRepository = questionRepository;
    this.calculationService = calculationService;
  }



  // 🔹 Получить вопросы по теме (без учета пола и возраста)
  public List<Question> getQuestionsByTopic(String topic) {
    System.out.println("🔍 Запрос вопросов по теме:");
    System.out.println("   Тема: " + topic);

    List<Question> allQuestions = new ArrayList<>();

    // Добавляем специфичные вопросы для выбранной темы
    List<Question> topicQuestions = new ArrayList<>();
    switch (topic) {
        case "iron":
            topicQuestions = createIronQuestions();
            break;
        case "energy":
            topicQuestions = createEnergyQuestions();
            break;
        case "sleep":
            topicQuestions = createSleepQuestions();
            break;
        case "weight":
            topicQuestions = createWeightQuestions();
            break;
        case "skin":
            topicQuestions = createSkinQuestions();
            break;
        case "digestion":
            topicQuestions = createDigestionQuestions();
            break;
        case "joints":
            topicQuestions = createJointsQuestions();
            break;
        case "immunity":
            topicQuestions = createImmunityQuestions();
            break;
        case "heart":
            topicQuestions = createHeartQuestions();
            break;
        case "thyroid":
            topicQuestions = createThyroidQuestions();
            break;
        case "female":
            topicQuestions = createFemaleQuestions();
            break;
        case "menopause":
            topicQuestions = createMenopauseQuestions();
            break;
        case "male":
            topicQuestions = createMaleQuestions();
            break;
        default:
            System.out.println("   ❌ Неизвестная тема: " + topic);
            return new ArrayList<>();
    }

    allQuestions.addAll(topicQuestions);

    // Добавляем общие вопросы для всех тем
    List<Question> generalQuestions = createGeneralQuestions();
    allQuestions.addAll(generalQuestions);

    System.out.println("   Найдено вопросов: " + allQuestions.size());
    System.out.println("   - Специфичных: " + topicQuestions.size());
    System.out.println("   - Общих: " + generalQuestions.size());

    return allQuestions;
  }

  // 🔹 Генерация рекомендаций по ответам пользователя (старая логика)
  public List<String> generateRecommendations(List<UserAnswer> answers) {
    // Простейшая логика: собираем ключи по которым были "Да"
    List<String> recommendations = new ArrayList<>();

    for (UserAnswer answer : answers) {
      if ("Да".equalsIgnoreCase(answer.getAnswer())) {
        recommendations.add("Рекомендация на основе: " + answer.getQuestionId());
      }
    }

    // Если ничего не набралось
    if (recommendations.isEmpty()) {
      recommendations.add("Нет явных признаков — рекомендуем профилактический приём базовых добавок.");
    }

    return recommendations;
  }

  // 🔹 Новая система рекомендаций с баллами (без учета пола и возраста)
  public RecommendationCalculationService.RecommendationResult generateAdvancedRecommendations(
      List<UserAnswer> answers, String selectedTopic) {
    
    return calculationService.calculateRecommendations(answers, selectedTopic);
  }

  // 🔹 Получить все доступные темы для выбора (согласно ТЗ)
  public List<String> getAvailableTopics() {
    return Arrays.asList(
      "energy",           // Бодрость и энергия
      "sleep",            // Крепкий сон, меньше стресса
      "weight",           // Контроль веса и аппетита
      "skin",             // Чистая кожа, крепкие волосы
      "digestion",        // Комфорт пищеварения
      "joints",           // Подвижные суставы, крепкие кости
      "immunity",         // Сильный иммунитет
      "heart",            // Здоровое сердце и сосуды
      "thyroid",          // Поддержка щитовидной железы
      "cycle",            // Регулярный цикл, мягкий ПМС
      "menopause",        // Менопауза без приливов
      "prostate",         // Мужское здоровье и простата
      "iron"              // Поднять гемоглобин
    );
  }

  // 🔹 Получить рекомендации по тегам
  public List<String> getRecommendationsByTags(Set<String> tags) {
    // Простая логика рекомендаций по тегам
    List<String> recommendations = new ArrayList<>();
    
    if (tags.contains("energy")) {
      recommendations.add("Energy - для повышения энергии и бодрости");
      recommendations.add("Coenzyme Q10 - для поддержки энергетического обмена");
    }
    
    if (tags.contains("sleep")) {
      recommendations.add("Magnesium B6 - для улучшения сна");
      recommendations.add("5-HTP 100 мг - для нормализации сна");
    }
    
    if (tags.contains("weight")) {
              // Berberine + Betulin удален из системы - больше не продается
      recommendations.add("Appetite Control - для контроля аппетита");
    }
    
    if (recommendations.isEmpty()) {
      recommendations.add("Нет явных признаков — рекомендуем профилактический приём базовых добавок.");
    }
    
    return recommendations;
  }

  // 🔹 Создать специфичные вопросы для темы "iron" (гемоглобин)
  private List<Question> createIronQuestions() {
    List<Question> questions = new ArrayList<>();
    
    // Вопрос 1: Диагностика врача
    Question question1 = new Question();
    question1.setId("iron_deficiency_doctor");
    question1.setText("Сообщал ли вам врач о низких запасах железа или снижении гемоглобина?");
    question1.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question1);
    
    // Вопрос 2: Симптомы слабости
    Question question2 = new Question();
    question2.setId("weakness_fatigue");
    question2.setText("Бывает ли у вас слабость, бледность, утомляемость при подъёме по лестнице?");
    question2.setOptions(Arrays.asList("нет", "немного", "сильно"));
    questions.add(question2);
    
    // Вопрос 3: Головокружения
    Question question3 = new Question();
    question3.setId("dizziness_shortness");
    question3.setText("Возникают ли головокружения при вставании или одышка при лёгкой нагрузке?");
    question3.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question3);
    
    // Вопрос 4: Кровопотери
    Question question4 = new Question();
    question4.setId("blood_loss");
    question4.setText("Были ли недавно донорства или кровопотери (у женщин — обильные менструации)?");
    question4.setOptions(Arrays.asList("нет", "было", "да, регулярно"));
    questions.add(question4);
    
    // Вопрос 5: Вегетарианство/веганство
    Question question5 = new Question();
    question5.setId("vegetarian_vegan");
    question5.setText("Придерживаетесь ли вегетарианского/веганского питания?");
    question5.setOptions(Arrays.asList("нет", "да"));
    questions.add(question5);
    
    // Вопрос 6: Чувствительность ЖКТ
    Question question6 = new Question();
    question6.setId("gut_sensitivity");
    question6.setText("Есть ли у вас повышенная чувствительность ЖКТ (тошнота, дискомфорт, гастрит, непереносимость некоторых добавок)?");
    question6.setOptions(Arrays.asList("нет", "немного", "выраженно"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'iron': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Бодрость и энергия"
  private List<Question> createEnergyQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("morning_energy");
    question1.setText("Сложно ли вам по утрам быстро \"прийти в себя\", включаться в дела?");
    question1.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("afternoon_crash");
    question2.setText("Бывает ли, что после углеводного обеда клонит в сон и падает продуктивность?");
    question2.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("post_infection_fatigue");
    question3.setText("Сохранялась ли усталость более 4 недель после инфекции (напр., COVID-19)?");
    question3.setOptions(Arrays.asList("нет", "было, но прошло", "да, держится"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("exercise_fatigue");
    question4.setText("Случается ли, что на тренировках быстро устаёте или тяжело подниматься по лестнице?");
    question4.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("iron_anemia_doctor");
    question5.setText("Сообщал ли врач о низких запасах железа или признаках анемии?");
    question5.setOptions(Arrays.asList("нет", "не знаю", "да"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("caffeine_sensitivity");
    question6.setText("Замечаете ли усиление тревоги или сердцебиения после кофе/энергетиков?");
    question6.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'energy': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Крепкий сон, меньше стресса"
  private List<Question> createSleepQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("sleep_onset");
    question1.setText("Часто ли вы засыпаете более 30 минут?");
    question1.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("night_awakenings");
    question2.setText("Бывает ли что вы просыпаетесь ночью чаще 1 раза?");
    question2.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("chronic_stress");
    question3.setText("Испытываете ли в последнее время продолжительный стресс (работа/личное)?");
    question3.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("low_motivation");
    question4.setText("Замечаете ли в последние недели снижение мотивации/\"ничего не хочется\"?");
    question4.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("screen_before_bed");
    question5.setText("Пользуетесь ли вы телефоном / планшетом / в течение часа до сна?");
    question5.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("stress_impact");
    question6.setText("Были ли у вас периоды длительного стресса, отразившиеся на самочувствии?");
    question6.setOptions(Arrays.asList("нет", "скорее да", "да"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'sleep': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Контроль веса и аппетита"
  private List<Question> createWeightQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("portion_control");
    question1.setText("Сложно ли вам остановиться на одной порции, бывает переедание?");
    question1.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("night_snacking");
    question2.setText("Случаются ли ночные перекусы или \"заедание\" стресса?");
    question2.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("late_dinner");
    question3.setText("Часто ли ужин приходится менее чем за 2 часа до сна?");
    question3.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("skip_breakfast");
    question4.setText("Бывает ли, что вы пропускаете завтрак 3 и более раз в неделю?");
    question4.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("bloating_sugar_cravings");
    question5.setText("Бывает ли сочетание: вздутие/тяжесть и выраженная тяга к сладкому?");
    question5.setOptions(Arrays.asList("нет", "иногда", "почти всегда"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("waist_increase");
    question6.setText("Есть ли тенденция к увеличению объёма талии?");
    question6.setOptions(Arrays.asList("нет", "немного", "выраженно"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'weight': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Чистая кожа, крепкие волосы"
  private List<Question> createSkinQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("deep_acne");
    question1.setText("Возникают ли болезненные глубокие высыпания (подкожные элементы)?");
    question1.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("acne_marks");
    question2.setText("Долго ли сохраняются пятна после высыпаний, неровный тон?");
    question2.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("wrinkles_elasticity");
    question3.setText("Замечаете ли мелкие морщины или снижение упругости?");
    question3.setOptions(Arrays.asList("нет", "немного", "выраженно"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("hair_loss");
    question4.setText("Отмечаете ли усиленное выпадение волос?");
    question4.setOptions(Arrays.asList("нет", "немного", "заметно"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("scalp_issues");
    question5.setText("Беспокоит ли перхоть/зуд кожи головы или повышенная жирность корней?");
    question5.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("skin_irritation");
    question6.setText("Часто ли появляется раздражение кожи /себодерматит?");
    question6.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'skin': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Комфорт пищеварения"
  private List<Question> createDigestionQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("recent_antibiotics");
    question1.setText("Принимали ли вы антибиотики за последние 6 месяцев?");
    question1.setOptions(Arrays.asList("нет", "да", "не помню"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("dairy_intolerance");
    question2.setText("Замечаете ли, что молочные продукты ухудшают самочувствие (возникает вздутие)?");
    question2.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("post_meal_discomfort");
    question3.setText("Возникают ли тяжесть или дискомфорт после еды?");
    question3.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("ibs_diagnosis");
    question4.setText("Сообщал ли врач о синдроме раздражённого кишечника или возможном воспалении?");
    question4.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("stress_gut_symptoms");
    question5.setText("Усиливаются ли ЖКТ-симптомы на фоне стресса?");
    question5.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("bloating_sweet_cravings");
    question6.setText("Бывает ли одновременно вздутие и выраженная тяга к сладкому?");
    question6.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'digestion': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Подвижные суставы, крепкие кости"
  private List<Question> createJointsQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("regular_exercise");
    question1.setText("Есть ли у вас регулярные беговые/прыжковые/силовые тренировки?");
    question1.setOptions(Arrays.asList("нет", "1-2 раза в неделю", "3 и более раз в неделю"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("recent_injuries");
    question2.setText("Были ли за последний год травмы или растяжения колен/голеностопа?");
    question2.setOptions(Arrays.asList("нет", "да", "повторные"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("joint_dryness");
    question3.setText("Есть ли ощущение \"сухости\" в суставах (часто вместе с сухой кожей)?");
    question3.setOptions(Arrays.asList("нет", "немного", "выраженно"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("age_category");
    question4.setText("Выберите ваш возраст?");
    question4.setOptions(Arrays.asList("до 35 лет", "35–49 лет", "50+ лет"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("knee_pain");
    question5.setText("Бывает ли боль в коленях к вечеру (на фоне нагрузки/массы тела)?");
    question5.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("bone_density_risk");
    question6.setText("Указывал ли врач на риски снижения плотности костей?");
    question6.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'joints': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Сильный иммунитет"
  private List<Question> createImmunityQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("social_contact");
    question1.setText("Часто ли вы контактируете с детьми или большим кругом людей?");
    question1.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("prolonged_colds");
    question2.setText("Тянутся ли простуды дольше 10 дней?");
    question2.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("frequent_infections");
    question3.setText("Случается ли, что простуды идут одна за другой (включая герпес)?");
    question3.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("cold_extremities");
    question4.setText("Бывает ли, что мёрзнут кисти/стопы или возникают головокружения?");
    question4.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("autoimmune_thyroid");
    question5.setText("Указывал ли врач (у вас/в семье) на аутоиммунную щитовидку/узлы?");
    question5.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("sleep_deprivation");
    question6.setText("Часто ли вы спите менее 6 часов?");
    question6.setOptions(Arrays.asList("почти никогда", "1-2 раза в неделю", "3-4 раза в неделю или чаще"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'immunity': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Здоровое сердце и сосуды"
  private List<Question> createHeartQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("family_cardiovascular");
    question1.setText("Были у вас или ваших родителей сердечно-сосудистые заболевания?");
    question1.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("exercise_fatigue_heart");
    question2.setText("Возникает ли у вас при нагрузке быстрая утомляемость или одышка?");
    question2.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("homocysteine_elevated");
    question3.setText("Было ли когда-нибудь в ваших анализах повышенное значение гомоцистеина?");
    question3.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("inflammation_joints");
    question4.setText("Бывает ли, что суставы болят именно из-за воспаления, или вам говорили о повышенных показателях воспаления в анализах?");
    question4.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("sedentary_lifestyle");
    question5.setText("Проводите ли большую часть дня сидя (больше 8 часов)?");
    question5.setOptions(Arrays.asList("нет", "иногда", "да"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("circulation_issues");
    question6.setText("Бывают ли у вас холодные кисти/стопы и эпизоды головокружения?");
    question6.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'heart': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Поддержка щитовидной железы"
  private List<Question> createThyroidQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("cold_sensitivity");
    question1.setText("Чувствительны ли вы к холоду, сложно ли вам \"включаться\" по утрам?");
    question1.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("tsh_elevated");
    question2.setText("Говорил ли врач, что у вас повышен ТТГ (гормон щитовидной железы), или вы принимаете L-тироксин/левотироксин?");
    question2.setOptions(Arrays.asList("нет", "не знаю", "да"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("autoimmune_thyroid_specific");
    question3.setText("Указывал ли врач на аутоиммунные процессы щитовидной железы?");
    question3.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("constipation");
    question4.setText("Бывают ли у вас запоры?");
    question4.setOptions(Arrays.asList("нет", "да"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("voice_neck_discomfort");
    question5.setText("Случается ли у вас охриплость голоса или дискомфорт в области шеи?");
    question5.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("evening_energy_drop");
    question6.setText("Бывает ли снижение энергии и настроения к вечеру?");
    question6.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'thyroid': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Регулярный цикл, мягкий ПМС"
  private List<Question> createFemaleQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("irregular_cycle");
    question1.setText("Бывает ли, что менструальный цикл длиннее 35 дней или приходит нерегулярно?");
    question1.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("pms_symptoms");
    question2.setText("Насколько выражены проявления ПМС (отёки, нагрубание груди, перепады настроения)?");
    question2.setOptions(Arrays.asList("нет", "умеренно", "сильно"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("premenstrual_acne");
    question3.setText("Перед менструацией усиливаются высыпания или жирность кожи?");
    question3.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("menstrual_pain");
    question4.setText("Бывают ли выраженные спазмы/болезненность во время менструации?");
    question4.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("heavy_periods");
    question5.setText("Бывают ли обильные менструации со сгустками?");
    question5.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("pregnancy_planning");
    question6.setText("Планируете ли беременность и есть ли сомнения в овуляции?");
    question6.setOptions(Arrays.asList("нет", "да"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'female': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Менопауза без приливов"
  private List<Question> createMenopauseQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("hot_flashes_frequent");
    question1.setText("Случаются ли приливы 4 и более раз в день?");
    question1.setOptions(Arrays.asList("нет", "да"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("night_hot_flashes");
    question2.setText("Мешают ли ночные приливы сну?");
    question2.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("mood_changes_menopause");
    question3.setText("Стали ли чаще проявляться раздражительность или перепады настроения?");
    question3.setOptions(Arrays.asList("нет", "немного", "сильно"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("joint_pain_menopause");
    question4.setText("Усилились ли боли в суставах после наступления менопаузы?");
    question4.setOptions(Arrays.asList("нет", "немного", "выраженно"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("weight_gain_menopause");
    question5.setText("Есть ли тенденция к увеличению объёма талии после менопаузы?");
    question5.setOptions(Arrays.asList("нет", "немного", "заметно"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("family_hormone_cancer");
    question6.setText("Известно ли вам о случаях гормонозависимых заболеваний у близких родственников (например, рак молочной железы, матки, яичников, простаты)?");
    question6.setOptions(Arrays.asList("нет", "да", "не знаю"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'menopause': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Вопросы для темы "Мужское здоровье"
  private List<Question> createMaleQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("urinary_symptoms");
    question1.setText("Стала ли струя мочи слабее, участились ли ночные позывы?");
    question1.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("energy_libido_decline");
    question2.setText("Замечали ли вы снижение энергии и либидо на фоне стресса?");
    question2.setOptions(Arrays.asList("нет", "немного", "сильно"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("prostate_discomfort");
    question3.setText("Случается ли тянущая боль или чувство давления в области простаты (внизу живота/промежности)?");
    question3.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("male_hair_loss");
    question4.setText("Усилилось ли в последнее время выпадение волос?");
    question4.setOptions(Arrays.asList("нет", "немного", "выраженно"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("recovery_time");
    question5.setText("Требуется ли больше времени на восстановление после нагрузок?");
    question5.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("red_meat_consumption");
    question6.setText("Часто ли вы едите красное мясо (говядина/свинина)?");
    question6.setOptions(Arrays.asList("никогда", "1-4 раза в неделю", "каждый день"));
    questions.add(question6);
    
    System.out.println("✅ Созданы специфичные вопросы для темы 'male': " + questions.size() + " вопросов");
    
    return questions;
  }

  // Общие вопросы для всех тем
  private List<Question> createGeneralQuestions() {
    List<Question> questions = new ArrayList<>();
    
    Question question1 = new Question();
    question1.setId("fish_consumption");
    question1.setText("Как часто вы употребляете рыбу/морепродукты?");
    question1.setOptions(Arrays.asList("почти никогда", "реже 1 раза в неделю", "1–2 раза в неделю", "3+ раз в неделю"));
    questions.add(question1);
    
    Question question2 = new Question();
    question2.setId("coffee_daily");
    question2.setText("Сколько чашек кофе вы выпиваете в день?");
    question2.setOptions(Arrays.asList("0", "1", "2–3", "4+"));
    questions.add(question2);
    
    Question question3 = new Question();
    question3.setId("physical_activity");
    question3.setText("Какой у вас уровень физической активности?");
    question3.setOptions(Arrays.asList("нет регулярных тренировок", "1–2 тренировки в неделю", "3–4", "5+"));
    questions.add(question3);
    
    Question question4 = new Question();
    question4.setId("smoking_vaping");
    question4.setText("Курите ли вы сейчас или используете вейп?");
    question4.setOptions(Arrays.asList("нет", "иногда", "регулярно"));
    questions.add(question4);
    
    Question question5 = new Question();
    question5.setId("sweet_cravings");
    question5.setText("Часто ли вас тянет на сладкое/рафинированные углеводы?");
    question5.setOptions(Arrays.asList("нет", "редко", "иногда", "часто"));
    questions.add(question5);
    
    Question question6 = new Question();
    question6.setId("digestive_issues");
    question6.setText("Бывает ли вздутие или нерегулярный стул 3+ дней в неделю?");
    question6.setOptions(Arrays.asList("нет", "иногда", "часто"));
    questions.add(question6);
    
    Question question7 = new Question();
    question7.setId("cold_frequency");
    question7.setText("Сколько раз болели ОРВИ/простудой за последний год?");
    question7.setOptions(Arrays.asList("0–1", "2–3", "4+"));
    questions.add(question7);
    
    System.out.println("✅ Созданы общие вопросы: " + questions.size() + " вопросов");
    
    return questions;
  }
}

