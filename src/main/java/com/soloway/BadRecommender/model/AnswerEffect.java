package com.soloway.BadRecommender.model;

import com.soloway.BadRecommender.model.enums.AnswerEffectType;
import com.soloway.BadRecommender.model.enums.Topic;

public class AnswerEffect {

  private Topic topic;                     // На какую тему влияет
  private AnswerEffectType effectType;     // Тип влияния: ADD, SET, EXCLUDE
  private int value;                       // Значение влияния (если применимо)

  public AnswerEffect(Topic topic, AnswerEffectType effectType, int value) {
    this.topic = topic;
    this.effectType = effectType;
    this.value = value;
  }

  public Topic getTopic() {
    return topic;
  }

  public void setTopic(Topic topic) {
    this.topic = topic;
  }

  public AnswerEffectType getEffectType() {
    return effectType;
  }

  public void setEffectType(AnswerEffectType effectType) {
    this.effectType = effectType;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public int getScore() {
    return this.value;
  }
}
