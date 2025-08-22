package com.soloway.BadRecommender.model.enums;

public enum QuestionType {
  BASE,    // пол, возраст
  TOPIC,   // тема и уточняющие вопросы
  FACTOR,  // общие факторы (кофе, тренировки и т.д.)
  PROFILE,    // Вопросы для сбора информации о пользователе
  MEDICAL     // Медицинские диагностические вопросы
}