package com.soloway.BadRecommender.model;


import com.soloway.BadRecommender.model.enums.Gender;
import com.soloway.BadRecommender.model.enums.QuestionType;
import java.util.List;

public class Question {

  private String id;
  private String text;
  private List<String> options;
  private boolean relevant;
  private Gender gender;
  private Integer minAge;
  private QuestionType type;
  private List<AnswerEffect> effects;

  // Пустой конструктор
  public Question() {}

  public Question(
      String id,
      String text,
      List<String> options,
      boolean relevant,
      Gender gender,
      Integer minAge,
      QuestionType type,
      List<AnswerEffect> effects
  ) {
    this.id = id;
    this.text = text;
    this.options = options;
    this.relevant = relevant;
    this.gender = gender;
    this.minAge = minAge;
    this.type = type;
    this.effects = effects;
  }

  // Геттеры и сеттеры
  public String getId() { return id; }

  public String getText() { return text; }

  public List<String> getOptions() { return options; }

  public boolean isRelevant() { return relevant; }

  public Gender getGender() { return gender; }

  public Integer getMinAge() { return minAge; }

  public QuestionType getType() { return type; }

  public List<AnswerEffect> getEffects() { return effects; }

  public void setId(String id) { this.id = id; }
  public void setText(String text) { this.text = text; }
  public void setOptions(List<String> options) { this.options = options; }
  public void setRelevant(boolean relevant) { this.relevant = relevant; }
  public void setGender(Gender gender) { this.gender = gender; }
  public void setMinAge(Integer minAge) { this.minAge = minAge; }
  public void setType(QuestionType type) { this.type = type; }
  public void setEffects(List<AnswerEffect> effects) { this.effects = effects; }
}