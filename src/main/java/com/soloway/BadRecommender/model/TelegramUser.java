package com.soloway.BadRecommender.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Модель пользователя Telegram для хранения состояния опроса
 */
public class TelegramUser {
    private Long chatId;
    private String username;
    private String firstName;
    private String lastName;
    private UserState state;
    private int currentQuestionIndex;
    private Map<Integer, String> answers;
    private LocalDateTime lastActivity;
    private String email;
    private boolean surveyCompleted;

    public enum UserState {
        START,
        WAITING_FOR_EMAIL,
        SURVEY_IN_PROGRESS,
        SURVEY_COMPLETED
    }

    public TelegramUser(Long chatId) {
        this.chatId = chatId;
        this.state = UserState.START;
        this.currentQuestionIndex = 0;
        this.answers = new HashMap<>();
        this.lastActivity = LocalDateTime.now();
        this.surveyCompleted = false;
    }

    // Геттеры и сеттеры
    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSurveyCompleted() {
        return surveyCompleted;
    }

    public void setSurveyCompleted(boolean surveyCompleted) {
        this.surveyCompleted = surveyCompleted;
    }

    // Вспомогательные методы
    public void addAnswer(int questionIndex, String answer) {
        this.answers.put(questionIndex, answer);
        this.lastActivity = LocalDateTime.now();
    }

    public void nextQuestion() {
        this.currentQuestionIndex++;
        this.lastActivity = LocalDateTime.now();
    }

    public void resetSurvey() {
        this.currentQuestionIndex = 0;
        this.answers.clear();
        this.state = UserState.START;
        this.surveyCompleted = false;
        this.lastActivity = LocalDateTime.now();
    }

    public boolean isActive() {
        return LocalDateTime.now().minusMinutes(30).isBefore(this.lastActivity);
    }
}
