package com.soloway.BadRecommender.model;

import java.util.Objects;

public class SupplementScore {
    private String supplementName;
    private int currentScore = 0;
    private int initialScore = 0;
    private boolean excluded = false;

    public SupplementScore(String supplementName, int initialScore) {
        this.supplementName = supplementName;
        this.initialScore = initialScore;
        this.currentScore = initialScore;
    }

    // 🔹 Добавить баллы к текущему счету
    public void addScore(int value) {
        if (!excluded) {
            this.currentScore += value;
        }
    }

    // 🔹 Установить конкретное значение баллов
    public void setScore(int value) {
        this.currentScore = value;
    }

    // 🔹 Исключить добавку (обнулить баллы)
    public void exclude() {
        this.excluded = true;
        this.currentScore = 0;
    }

    // 🔹 Сбросить исключение
    public void include() {
        this.excluded = false;
        this.currentScore = initialScore;
    }

    // 🔹 Получить финальный балл (учитывая исключения)
    public int getFinalScore() {
        return excluded ? 0 : currentScore;
    }

    // Геттеры и сеттеры
    public String getSupplementName() { return supplementName; }
    public int getCurrentScore() { return currentScore; }
    public int getInitialScore() { return initialScore; }
    public boolean isExcluded() { return excluded; }

    public void setSupplementName(String supplementName) { this.supplementName = supplementName; }
    public void setCurrentScore(int currentScore) { this.currentScore = currentScore; }
    public void setInitialScore(int initialScore) { this.initialScore = initialScore; }
    public void setExcluded(boolean excluded) { this.excluded = excluded; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplementScore)) return false;
        SupplementScore that = (SupplementScore) o;
        return Objects.equals(supplementName, that.supplementName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplementName);
    }

    @Override
    public String toString() {
        return "SupplementScore{" +
                "supplementName='" + supplementName + '\'' +
                ", currentScore=" + currentScore +
                ", initialScore=" + initialScore +
                ", excluded=" + excluded +
                '}';
    }
}

