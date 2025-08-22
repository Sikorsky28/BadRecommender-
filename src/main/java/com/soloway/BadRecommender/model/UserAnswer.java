package com.soloway.BadRecommender.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserAnswer {
  private String gender;
  private int age;
  private Map<String, String> selectedTopics = new HashMap<>();
  private Map<String, String> followUpAnswers = new HashMap<>();
  private String questionId;
  private String answer;

  // Getters and setters


  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Map<String, String> getSelectedTopics() {
    return selectedTopics;
  }

  public void setSelectedTopics(Map<String, String> selectedTopics) {
    this.selectedTopics = selectedTopics;
  }

  public Map<String, String> getFollowUpAnswers() {
    return followUpAnswers;
  }

  public void setFollowUpAnswers(Map<String, String> followUpAnswers) {
    this.followUpAnswers = followUpAnswers;
  }

  // ✅ Удобные методы

  public void addSelectedTopic(String topic, String answer) {
    selectedTopics.put(topic, answer);
  }

  public void addFollowUpAnswer(String questionId, String answer) {
    followUpAnswers.put(questionId, answer);
  }

  public String getSelectedTopicAnswer(String topic) {
    return selectedTopics.getOrDefault(topic, "");
  }

  public String getFollowUpAnswer(String questionId) {
    return followUpAnswers.getOrDefault(questionId, "");
  }

  @Override
  public String toString() {
    return "UserAnswer{" +
        "gender='" + gender + '\'' +
        ", age=" + age +
        ", selectedTopics=" + selectedTopics +
        ", followUpAnswers=" + followUpAnswers +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserAnswer)) return false;
    UserAnswer that = (UserAnswer) o;
    return age == that.age &&
        Objects.equals(gender, that.gender) &&
        Objects.equals(selectedTopics, that.selectedTopics) &&
        Objects.equals(followUpAnswers, that.followUpAnswers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gender, age, selectedTopics, followUpAnswers);
  }
}
