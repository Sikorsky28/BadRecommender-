package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.config.TelegramBotConfig;
import com.soloway.BadRecommender.model.TelegramUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис Telegram бота для проведения опросов
 */
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    private final TelegramBotConfig botConfig;
    private final TelegramUserService userService;
    private final RecommendationService recommendationService;
    private final EmailService emailService;

    @Autowired
    public TelegramBotService(TelegramBotConfig botConfig, 
                            TelegramUserService userService,
                            RecommendationService recommendationService,
                            EmailService emailService) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.recommendationService = recommendationService;
        this.emailService = emailService;
        
        logger.info("TelegramBotService создан");
    }

    @PostConstruct
    public void init() {
        logger.info("Инициализация Telegram бота...");
        logger.info("Bot enabled: {}", botConfig.isBotEnabled());
        logger.info("Bot token: {}", botConfig.getBotToken() != null ? "***" : "null");
        logger.info("Bot username: {}", botConfig.getBotUsername());
    }

    @Override
    public String getBotToken() {
        String token = botConfig.getBotToken();
        logger.debug("Получен токен бота: {}", token != null ? "***" : "null");
        return token;
    }

    @Override
    public String getBotUsername() {
        String username = botConfig.getBotUsername();
        logger.debug("Получен username бота: {}", username);
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Получено обновление: {}", update);
        
        if (!botConfig.isBotEnabled()) {
            logger.warn("Telegram bot is disabled");
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();

            logger.info("Received message from {} ({}): {}", username, chatId, messageText);

            try {
                handleMessage(chatId, username, firstName, lastName, messageText);
            } catch (Exception e) {
                logger.error("Error handling message from {}: {}", chatId, e.getMessage(), e);
                sendMessage(chatId, "Произошла ошибка. Попробуйте позже или начните заново с команды /start");
            }
        } else {
            logger.debug("Получено обновление без текстового сообщения: {}", update);
        }
    }

    private void handleMessage(Long chatId, String username, String firstName, String lastName, String messageText) {
        logger.info("Обработка сообщения от {}: {}", username, messageText);
        
        TelegramUser user = userService.getUser(chatId);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        if ("/start".equals(messageText)) {
            logger.info("Обработка команды /start для пользователя {}", username);
            handleStartCommand(user);
        } else if ("/help".equals(messageText)) {
            logger.info("Обработка команды /help для пользователя {}", username);
            handleHelpCommand(user);
        } else if ("/reset".equals(messageText)) {
            logger.info("Обработка команды /reset для пользователя {}", username);
            handleResetCommand(user);
        } else {
            logger.info("Обработка обычного сообщения от {}: {}", username, messageText);
            handleRegularMessage(user, messageText);
        }

        userService.updateUser(user);
    }

    private void handleStartCommand(TelegramUser user) {
        logger.info("Выполнение команды /start для пользователя {}", user.getUsername());
        
        user.resetSurvey();
        user.setState(TelegramUser.UserState.WAITING_FOR_EMAIL);
        
        String welcomeMessage = "👋 Привет! Я бот для проведения опроса о здоровье.\n\n" +
                "Для начала мне нужен ваш email адрес, чтобы отправить результаты опроса.\n\n" +
                "Пожалуйста, отправьте ваш email:";
        
        logger.info("Отправка приветственного сообщения пользователю {}", user.getUsername());
        sendMessage(user.getChatId(), welcomeMessage);
    }

    private void handleHelpCommand(TelegramUser user) {
        String helpMessage = "🤖 Доступные команды:\n\n" +
                "/start - Начать опрос заново\n" +
                "/help - Показать эту справку\n" +
                "/reset - Сбросить текущий опрос\n\n" +
                "Если у вас возникли проблемы, попробуйте команду /reset";
        
        sendMessage(user.getChatId(), helpMessage);
    }

    private void handleResetCommand(TelegramUser user) {
        user.resetSurvey();
        user.setState(TelegramUser.UserState.WAITING_FOR_EMAIL);
        
        String resetMessage = "🔄 Опрос сброшен. Давайте начнем заново!\n\n" +
                "Пожалуйста, отправьте ваш email адрес:";
        
        sendMessage(user.getChatId(), resetMessage);
    }

    private void handleRegularMessage(TelegramUser user, String messageText) {
        switch (user.getState()) {
            case WAITING_FOR_EMAIL:
                handleEmailInput(user, messageText);
                break;
            case SURVEY_IN_PROGRESS:
                handleSurveyAnswer(user, messageText);
                break;
            case SURVEY_COMPLETED:
                handleCompletedSurvey(user, messageText);
                break;
            default:
                sendMessage(user.getChatId(), "Пожалуйста, используйте команду /start для начала опроса");
        }
    }

    private void handleEmailInput(TelegramUser user, String email) {
        if (isValidEmail(email)) {
            user.setEmail(email);
            user.setState(TelegramUser.UserState.SURVEY_IN_PROGRESS);
            user.setCurrentQuestionIndex(0);
            
            sendMessage(user.getChatId(), "✅ Email сохранен: " + email + "\n\n" +
                    "Теперь давайте начнем опрос! Отвечайте на вопросы, выбирая один из вариантов ответа.");
            
            sendNextQuestion(user);
        } else {
            sendMessage(user.getChatId(), "❌ Неверный формат email. Пожалуйста, введите корректный email адрес:");
        }
    }

    private void handleSurveyAnswer(TelegramUser user, String answer) {
        user.addAnswer(user.getCurrentQuestionIndex(), answer);
        user.nextQuestion();
        
        if (user.getCurrentQuestionIndex() >= 15) {
            completeSurvey(user);
        } else {
            sendNextQuestion(user);
        }
    }

    private void handleCompletedSurvey(TelegramUser user, String message) {
        if ("/start".equals(message)) {
            handleStartCommand(user);
        } else {
            sendMessage(user.getChatId(), "Опрос уже завершен. Используйте /start для нового опроса или /help для справки.");
        }
    }

    private void sendNextQuestion(TelegramUser user) {
        String questionText = "Вопрос " + (user.getCurrentQuestionIndex() + 1) + " из 15:\n\n" +
                "Как вы оцениваете свое общее самочувствие?";
        
        ReplyKeyboardMarkup keyboard = createAnswerKeyboard(
                "Отлично", "Хорошо", "Удовлетворительно", "Плохо"
        );
        
        sendMessageWithKeyboard(user.getChatId(), questionText, keyboard);
    }

    private void completeSurvey(TelegramUser user) {
        user.setState(TelegramUser.UserState.SURVEY_COMPLETED);
        user.setSurveyCompleted(true);
        
        String completionMessage = "🎉 Опрос завершен!\n\n" +
                "Спасибо за ваши ответы. Мы обрабатываем результаты и отправим их на ваш email: " + 
                user.getEmail() + "\n\n" +
                "Используйте /start для нового опроса или /help для справки.";
        
        sendMessage(user.getChatId(), completionMessage);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private ReplyKeyboardMarkup createAnswerKeyboard(String... options) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        keyboard.setSelective(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        
        for (String option : options) {
            row.add(option);
            if (row.size() == 2) {
                keyboardRows.add(row);
                row = new KeyboardRow();
            }
        }
        
        if (!row.isEmpty()) {
            keyboardRows.add(row);
        }
        
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }

    private void sendMessage(Long chatId, String text) {
        logger.info("Отправка сообщения в чат {}: {}", chatId, text);
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableMarkdown(true);
        
        try {
            execute(message);
            logger.info("Сообщение успешно отправлено в чат {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        logger.info("Отправка сообщения с клавиатурой в чат {}: {}", chatId, text);
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableMarkdown(true);
        message.setReplyMarkup(keyboard);
        
        try {
            execute(message);
            logger.info("Сообщение с клавиатурой успешно отправлено в чат {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending message with keyboard to {}: {}", chatId, e.getMessage(), e);
        }
    }
}
