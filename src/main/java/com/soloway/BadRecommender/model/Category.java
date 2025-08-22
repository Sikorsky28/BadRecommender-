package com.soloway.BadRecommender.model;

import java.util.HashMap;
import java.util.Map;

public class Category {
  private String name;
  private Map<String, Integer> supplementScores = new HashMap<>();

  public Category(String name) {
    this.name = name;
  }

  public void addSupplement(String supplementName, int score) {
    supplementScores.put(supplementName, score);
  }

  public Map<String, Integer> getSupplementScores() {
    return supplementScores;
  }

  public String getName() {
    return name;
  }
}
