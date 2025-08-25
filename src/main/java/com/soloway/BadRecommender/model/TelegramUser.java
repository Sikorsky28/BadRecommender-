package com.soloway.BadRecommender.model;

import java.util.HashMap;
import java.util.Map;

public class TelegramUser {
    private Long chatId;
    private String userName;
    private String selectedTopic;
    private Map<String, String> answers;
    private int currentQuestionIndex;
    private String state; // "START", "NAME", "TOPIC", "QUESTIONS", "EMAIL", "COMPLETE"

    public TelegramUser(Long chatId) {
        this.chatId = chatId;
        this.answers = new HashMap<>();
        this.currentQuestionIndex = 0;
        this.state = "START";
    }

    // Геттеры и сеттеры
    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSelectedTopic() {
        return selectedTopic;
    }

    public void setSelectedTopic(String selectedTopic) {
        this.selectedTopic = selectedTopic;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void addAnswer(String questionId, String answer) {
        this.answers.put(questionId, answer);
    }

    public void nextQuestion() {
        this.currentQuestionIndex++;
    }

    public void reset() {
        this.answers.clear();
        this.currentQuestionIndex = 0;
        this.state = "START";
        this.userName = null;
        this.selectedTopic = null;
    }
}
