package com.soloway.BadRecommender.model;

import java.util.Map;

public class AnswerOption {
  private String text;
  private Map<String, Integer> effects;     // { "Magnesium B6": 2, "CoQ10": 1 }
  private Map<String, Integer> setValues;   // { "Iron bisglycinate": 0 } или {"Lactoferrin": -1} в случае exclude

  // Конструкторы, геттеры/сеттеры

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Map<String, Integer> getEffects() {
    return effects;
  }

  public void setEffects(Map<String, Integer> effects) {
    this.effects = effects;
  }

  public Map<String, Integer> getSetValues() {
    return setValues;
  }

  public void setSetValues(Map<String, Integer> setValues) {
    this.setValues = setValues;
  }
}
