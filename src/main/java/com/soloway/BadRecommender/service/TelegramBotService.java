package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.TelegramUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Autowired
    private RecommendationCalculationService recommendationService;

    @Autowired
    private EmailService emailService;

    private Map<Long, TelegramUser> userSessions = new HashMap<>();

    @Override
    public String getBotUsername() {
        return System.getenv("TELEGRAM_BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        TelegramUser user = getUserSession(chatId);

        switch (user.getState()) {
            case "START":
                handleStart(chatId, user);
                break;
            case "NAME":
                handleName(chatId, user, text);
                break;
            case "EMAIL":
                handleEmail(chatId, user, text);
                break;
            default:
                sendMessage(chatId, "Пожалуйста, используйте кнопки для навигации.");
        }
    }

    private void handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        TelegramUser user = getUserSession(chatId);

        if (data.startsWith("topic_")) {
            handleTopicSelection(chatId, user, data.substring(6));
        } else if (data.startsWith("answer_")) {
            handleQuestionAnswer(chatId, user, data.substring(7));
        }
    }

    private void handleStart(Long chatId, TelegramUser user) {
        String welcomeText = "🏥 Добро пожаловать в SOLOWAYS!\n\n" +
                "Я помогу вам подобрать персональные рекомендации БАДов.\n\n" +
                "Для начала, как к вам можно обращаться?";
        
        user.setState("NAME");
        sendMessage(chatId, welcomeText);
    }

    private void handleName(Long chatId, TelegramUser user, String name) {
        if (name.length() < 2) {
            sendMessage(chatId, "Пожалуйста, введите ваше имя (минимум 2 символа).");
            return;
        }

        user.setUserName(name);
        user.setState("TOPIC");
        
        String topicText = "Здравствуйте, " + name + "! 👋\n\n" +
                "Выберите интересующую вас тему:";
        
        sendTopicSelection(chatId, topicText);
    }

    private void handleTopicSelection(Long chatId, TelegramUser user, String topic) {
        user.setSelectedTopic(topic);
        user.setState("QUESTIONS");
        user.setCurrentQuestionIndex(0);
        
        sendNextQuestion(chatId, user);
    }

    private void handleQuestionAnswer(Long chatId, TelegramUser user, String answer) {
        // Сохраняем ответ
        String questionId = "q" + user.getCurrentQuestionIndex();
        user.addAnswer(questionId, answer);
        
        user.nextQuestion();
        
        // Проверяем, есть ли еще вопросы
        if (user.getCurrentQuestionIndex() < getQuestionsForTopic(user.getSelectedTopic()).size()) {
            sendNextQuestion(chatId, user);
        } else {
            // Все вопросы пройдены, запрашиваем email
            user.setState("EMAIL");
            sendMessage(chatId, "Отлично! Теперь введите ваш email для получения персональных рекомендаций:");
        }
    }

    private void handleEmail(Long chatId, TelegramUser user, String email) {
        if (!email.contains("@")) {
            sendMessage(chatId, "Пожалуйста, введите корректный email адрес.");
            return;
        }

        try {
            // Генерируем рекомендации
            List<String> mainRecommendations = recommendationService.getMainRecommendations(user.getSelectedTopic(), user.getAnswers());
            List<String> additionalRecommendations = recommendationService.getAdditionalRecommendations(user.getSelectedTopic(), user.getAnswers());
            
            // Отправляем email
            emailService.sendRecommendationsEmail(email, user.getUserName(), user.getSelectedTopic(), 
                    convertToSupplements(mainRecommendations), convertToSupplements(additionalRecommendations));
            
            user.setState("COMPLETE");
            
            String completionText = "✅ Спасибо за прохождение опроса!\n\n" +
                    "Персональные рекомендации отправлены на ваш email: " + email + "\n\n" +
                    "Для получения новых рекомендаций нажмите /start";
            
            sendMessage(chatId, completionText);
            
        } catch (Exception e) {
            sendMessage(chatId, "Произошла ошибка при отправке рекомендаций. Попробуйте позже.");
        }
    }

    private void sendTopicSelection(Long chatId, String text) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        String[] topics = {
            "energy", "sleep", "weight", "skin", "digestion", 
            "joints", "immunity", "heart", "thyroid", "female", 
            "menopause", "male", "iron"
        };
        
        String[] topicNames = {
            "⚡ Бодрость и энергия", "😴 Крепкий сон", "⚖️ Контроль веса", 
            "✨ Чистая кожа", "🫁 Комфорт пищеварения", "🦴 Подвижные суставы",
            "🛡️ Сильный иммунитет", "❤️ Здоровое сердце", "🦋 Щитовидная железа",
            "🌸 Женское здоровье", "🌺 Менопауза", "👨 Мужское здоровье", "🩸 Гемоглобин"
        };
        
        for (int i = 0; i < topics.length; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(topicNames[i]);
            button.setCallbackData("topic_" + topics[i]);
            row.add(button);
            rows.add(row);
        }
        
        keyboard.setKeyboard(rows);
        sendMessageWithKeyboard(chatId, text, keyboard);
    }

    private void sendNextQuestion(Long chatId, TelegramUser user) {
        List<Map<String, Object>> questions = getQuestionsForTopic(user.getSelectedTopic());
        Map<String, Object> question = questions.get(user.getCurrentQuestionIndex());
        
        String questionText = "Вопрос " + (user.getCurrentQuestionIndex() + 3) + " из 15:\n\n" +
                question.get("text").toString();
        
        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) question.get("options");
        
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        for (String option : options) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(option);
            button.setCallbackData("answer_" + option);
            row.add(button);
            rows.add(row);
        }
        
        keyboard.setKeyboard(rows);
        sendMessageWithKeyboard(chatId, questionText, keyboard);
    }

    private TelegramUser getUserSession(Long chatId) {
        return userSessions.computeIfAbsent(chatId, TelegramUser::new);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML");
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    private void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML");
        message.setReplyMarkup(keyboard);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения с клавиатурой: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> getQuestionsForTopic(String topic) {
        // Здесь должна быть логика получения вопросов из RecommendationCalculationService
        // Пока возвращаем заглушку
        return new ArrayList<>();
    }

    private List<com.soloway.BadRecommender.model.Supplement> convertToSupplements(List<String> supplementNames) {
        // Конвертируем названия в объекты Supplement
        List<com.soloway.BadRecommender.model.Supplement> supplements = new ArrayList<>();
        for (String name : supplementNames) {
            com.soloway.BadRecommender.model.Supplement supplement = new com.soloway.BadRecommender.model.Supplement();
            supplement.setName(name);
            supplements.add(supplement);
        }
        return supplements;
    }
}
