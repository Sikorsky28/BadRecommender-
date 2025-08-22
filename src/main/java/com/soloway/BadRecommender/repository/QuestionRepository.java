package com.soloway.BadRecommender.repository;

import com.soloway.BadRecommender.model.AnswerEffect;
import com.soloway.BadRecommender.model.Question;
import com.soloway.BadRecommender.model.enums.AnswerEffectType;
import com.soloway.BadRecommender.model.enums.Gender;
import com.soloway.BadRecommender.model.enums.QuestionType;
import com.soloway.BadRecommender.model.enums.Topic;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class QuestionRepository {

  private final List<Question> questions = new ArrayList<>();

  @PostConstruct
  public void init() {

    // 🔹 Профильные вопросы
    questions.add(new Question(
        "general_gender",
        "Ваш пол?",
        List.of("Мужчина", "Женщина"),
        false,
        null,
        null,
        QuestionType.PROFILE,
        Collections.emptyList()
    ));

    questions.add(new Question(
        "general_age",
        "Ваш возраст?",
        List.of("до 25", "25–34", "35–44", "45–54", "55+"),
        false,
        null,
        null,
        QuestionType.PROFILE,
        Collections.emptyList()
    ));

    // 🔹 Энергия и бодрость
    questions.add(new Question(
        "energy_afternoon_sleep",
        "После обеда тянет в сон?",
        List.of("Иногда", "Почти каждый день", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.ENERGY, AnswerEffectType.ADD, 1),
            new AnswerEffect(Topic.ENERGY, AnswerEffectType.ADD, 2),
            new AnswerEffect(Topic.ENERGY, AnswerEffectType.ADD, 0)
        )
    ));

    questions.add(new Question(
        "coffee_consumption",
        "Сколько кофе в день пьете?",
        List.of("0-1 чашка", "2-3 чашки", "4+ чашки"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 0),
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 1),
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 2)
        )
    ));

    // 🔹 Сон и стресс
    questions.add(new Question(
        "sleep_duration",
        "Засыпаете дольше 30 минут?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 2)
        )
    ));

    questions.add(new Question(
        "anxiety_level",
        "Какой у вас уровень тревоги (0-10)?",
        List.of("0-6", "7-8", "9-10"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 0),
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 1),
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 2)
        )
    ));

    // 🔹 Контроль веса
    questions.add(new Question(
        "weight_difficulty",
        "Главная трудность на диете?",
        List.of("Голод", "Тяга к сладкому", "Нет трудностей"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.WEIGHT_CONTROL, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.WEIGHT_CONTROL, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.WEIGHT_CONTROL, AnswerEffectType.ADD, 0)
        )
    ));

    // 🔹 Кожа и волосы
    questions.add(new Question(
        "dry_skin",
        "Сухая кожа?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.SKIN_HAIR, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.SKIN_HAIR, AnswerEffectType.ADD, 2)
        )
    ));

    questions.add(new Question(
        "hair_loss",
        "Выпадают волосы?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.SKIN_HAIR, AnswerEffectType.ADD, 2),
            new AnswerEffect(Topic.SKIN_HAIR, AnswerEffectType.ADD, 1)
        )
    ));

    // 🔹 Пищеварение
    questions.add(new Question(
        "bloating_frequency",
        "Вздутие живота 3+ раза в неделю?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.DIGESTION, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.DIGESTION, AnswerEffectType.ADD, 0)
        )
    ));

    // 🔹 Суставы и кости
    questions.add(new Question(
        "knee_pain",
        "Болят колени при спуске по лестнице?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.JOINTS_BONES, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.JOINTS_BONES, AnswerEffectType.ADD, 0)
        )
    ));

    // 🔹 Иммунитет
    questions.add(new Question(
        "frequent_colds",
        "Частые простуды (больше 4 в год)?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.IMMUNITY, AnswerEffectType.ADD, 2),
            new AnswerEffect(Topic.IMMUNITY, AnswerEffectType.ADD, 1)
        )
    ));

    // 🔹 Сердце и сосуды
    questions.add(new Question(
        "high_cholesterol",
        "ЛПНП больше 3 ммоль/л?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.HEART_VESSELS, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.HEART_VESSELS, AnswerEffectType.ADD, 0)
        )
    ));

    // 🔹 Щитовидная железа
    questions.add(new Question(
        "high_tsh",
        "ТТГ больше 2,5 мЕд/л?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.THYROID, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.THYROID, AnswerEffectType.ADD, 1)
        )
    ));

    // 🔹 Женское здоровье - цикл
    questions.add(new Question(
        "menstruation_cycle",
        "Цикл больше 35 дней?",
        List.of("Да", "Нет"),
        true,
        Gender.FEMALE,
        16,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.CYCLE_PMS, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.CYCLE_PMS, AnswerEffectType.ADD, 0)
        )
    ));

    // 🔹 Менопауза
    questions.add(new Question(
        "hot_flashes",
        "Приливы больше 4 раз в день?",
        List.of("Да", "Нет"),
        true,
        Gender.FEMALE,
        45,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.MENOPAUSE, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.MENOPAUSE, AnswerEffectType.ADD, 0)
        )
    ));

    // 🔹 Мужское здоровье
    questions.add(new Question(
        "prostate_issue",
        "Струя мочи стала слабее?",
        List.of("Да", "Нет"),
        true,
        Gender.MALE,
        30,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.MALE_HEALTH, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.MALE_HEALTH, AnswerEffectType.ADD, 0)
        )
    ));

    // 🔹 Гемоглобин
    questions.add(new Question(
        "low_ferritin",
        "Ферритин ниже нормы?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.HEMOGLOBIN, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.HEMOGLOBIN, AnswerEffectType.ADD, 0)
        )
    ));

    questions.add(new Question(
        "iron_intolerance",
        "Плохо переносите железо?",
        List.of("Да", "Нет"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.HEMOGLOBIN, AnswerEffectType.ADD, 3),
            new AnswerEffect(Topic.HEMOGLOBIN, AnswerEffectType.SET, 0)
        )
    ));

    // 🔹 Общие факторы
    questions.add(new Question(
        "exercise_frequency",
        "Сколько раз в неделю тренируетесь?",
        List.of("0-3 раза", "4+ раза"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 0),
            new AnswerEffect(Topic.SLEEP_STRESS, AnswerEffectType.ADD, 1),
            new AnswerEffect(Topic.ENERGY, AnswerEffectType.ADD, 1)
        )
    ));

    questions.add(new Question(
        "smoking_habit",
        "Курите или вейпите?",
        List.of("Нет", "5-9 сигарет/день", "10+ сигарет/день"),
        true,
        null,
        null,
        QuestionType.MEDICAL,
        List.of(
            new AnswerEffect(Topic.HEART_VESSELS, AnswerEffectType.ADD, 0),
            new AnswerEffect(Topic.HEART_VESSELS, AnswerEffectType.ADD, 1),
            new AnswerEffect(Topic.ENERGY, AnswerEffectType.ADD, 1),
            new AnswerEffect(Topic.IMMUNITY, AnswerEffectType.ADD, 1),
            new AnswerEffect(Topic.HEART_VESSELS, AnswerEffectType.ADD, 2),
            new AnswerEffect(Topic.ENERGY, AnswerEffectType.ADD, 2),
            new AnswerEffect(Topic.IMMUNITY, AnswerEffectType.ADD, 2)
        )
    ));
  }

  public List<Question> getAll() {
    return questions;
  }

  public List<Question> getApplicableQuestions(Gender gender, int age) {
    return questions.stream()
        .filter(q -> {
          if (q.getGender() != null && q.getGender() != gender) return false;
          if (q.getMinAge() != null && age < q.getMinAge()) return false;
          return true;
        })
        .collect(Collectors.toList());
  }

  public Optional<Question> getById(String id) {
    return questions.stream().filter(q -> q.getId().equals(id)).findFirst();
  }
}
