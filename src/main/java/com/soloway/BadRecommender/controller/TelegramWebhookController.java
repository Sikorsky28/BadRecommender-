package com.soloway.BadRecommender.controller;

import com.soloway.BadRecommender.config.TelegramBotConfig;
import com.soloway.BadRecommender.model.TelegramUser;
import com.soloway.BadRecommender.model.Supplement;
import com.soloway.BadRecommender.model.SupplementScore;
import com.soloway.BadRecommender.service.TelegramUserService;
import com.soloway.BadRecommender.service.TelegramSurveyService;
import com.soloway.BadRecommender.service.ScoreCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для обработки webhook от Telegram
 */
@RestController
@RequestMapping("/webhook")
public class TelegramWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(TelegramWebhookController.class);

    private final TelegramBotConfig botConfig;
    private final TelegramUserService userService;
    private final TelegramSurveyService surveyService;
    private final WebClient webClient;

    @Autowired
    public TelegramWebhookController(TelegramBotConfig botConfig, TelegramUserService userService, TelegramSurveyService surveyService) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.surveyService = surveyService;
        this.webClient = WebClient.builder().build();
        logger.info("TelegramWebhookController создан");
    }

    @PostMapping("/{token}")
    public ResponseEntity<String> handleWebhook(@PathVariable String token, @RequestBody Update update) {
        logger.info("=== ПОЛУЧЕНО WEBHOOK ОБНОВЛЕНИЕ ОТ TELEGRAM ===");
        logger.info("Token: {}", token);
        logger.info("Update: {}", update);

        // Проверяем токен
        if (!botConfig.getBotToken().equals(token)) {
            logger.warn("Неверный токен: {}", token);
            return ResponseEntity.badRequest().body("Invalid token");
        }

        if (!botConfig.isBotEnabled()) {
            logger.warn("Telegram bot is disabled");
            return ResponseEntity.ok("Bot disabled");
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();

            logger.info("✅ Received webhook message from {} ({}): {}", username, chatId, messageText);

            try {
                handleMessage(chatId, username, firstName, lastName, messageText);
                return ResponseEntity.ok("OK");
            } catch (Exception e) {
                logger.error("Error handling webhook message from {}: {}", chatId, e.getMessage(), e);
                return ResponseEntity.ok("Error handled");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String callbackQueryId = update.getCallbackQuery().getId();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String username = update.getCallbackQuery().getFrom().getUserName();
            String firstName = update.getCallbackQuery().getFrom().getFirstName();
            String lastName = update.getCallbackQuery().getFrom().getLastName();

            logger.info("✅ Received callback query from {} ({}): {}", username, chatId, callbackData);

            try {
                handleCallbackQuery(chatId, username, firstName, lastName, callbackData, callbackQueryId);
                return ResponseEntity.ok("OK");
            } catch (Exception e) {
                logger.error("Error handling callback query from {}: {}", chatId, e.getMessage(), e);
                return ResponseEntity.ok("Error handled");
            }
        } else {
            logger.debug("Получено webhook обновление без текстового сообщения: {}", update);
        }

        return ResponseEntity.ok("OK");
    }

    private void handleMessage(Long chatId, String username, String firstName, String lastName, String messageText) {
        logger.info("Обработка webhook сообщения от {}: {}", username, messageText);

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

    private void handleCallbackQuery(Long chatId, String username, String firstName, String lastName, String callbackData, String callbackQueryId) {
        logger.info("=== НАЧАЛО ОБРАБОТКИ CALLBACK QUERY ===");
        logger.info("Обработка callback query от {}: {}", username, callbackData);
        logger.info("ChatId: {}, CallbackQueryId: {}", chatId, callbackQueryId);

        TelegramUser user = userService.getUser(chatId);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        logger.info("Пользователь получен: {}, текущий индекс вопроса: {}", user.getUsername(), user.getCurrentQuestionIndex());

        // Проверяем специальные callback'и
        if ("NEW_SURVEY".equals(callbackData)) {
            logger.info("Пользователь {} нажал кнопку 'Новый опрос'", user.getUsername());
            handleStartCommand(user);
            userService.updateUser(user);
            answerCallbackQuery(callbackQueryId);
            return;
        }
        
        if ("GENETICS".equals(callbackData)) {
            logger.info("Пользователь {} нажал кнопку 'Генетика'", user.getUsername());
            handleGeneticsCommand(user);
            userService.updateUser(user);
            answerCallbackQuery(callbackQueryId);
            return;
        }

        // Обрабатываем ответ через сервис опроса
        logger.info("Вызываем surveyService.processAnswer с ответом: {}", callbackData);
        surveyService.processAnswer(user, callbackData);
        
        logger.info("После обработки callback: индекс={}, завершен={}", 
                   user.getCurrentQuestionIndex(), surveyService.isSurveyCompleted(user));
        
        // Проверяем, завершен ли опрос
        if (surveyService.isSurveyCompleted(user)) {
            logger.info("Опрос завершен для пользователя {}, показываем рекомендации", user.getUsername());
            completeSurvey(user);
        } else {
            logger.info("Отправляем следующий вопрос для пользователя {}", user.getUsername());
            sendNextQuestion(user);
        }

        logger.info("Обновляем пользователя в базе данных");
        userService.updateUser(user);
        
        // Отвечаем на callback query, чтобы убрать "часики" у кнопки
        logger.info("Отвечаем на callback query с ID: {}", callbackQueryId);
        answerCallbackQuery(callbackQueryId);
        
        logger.info("=== КОНЕЦ ОБРАБОТКИ CALLBACK QUERY ===");
    }

    private void handleStartCommand(TelegramUser user) {
        logger.info("Выполнение команды /start для пользователя {}", user.getUsername());

        // Полностью сбрасываем состояние пользователя
        user.resetSurvey();
        user.setState(TelegramUser.UserState.SURVEY_IN_PROGRESS);
        user.setSurveyCompleted(false);
        user.setCurrentQuestionIndex(0);
        user.setSelectedTopic(null);
        user.setAnswers(new HashMap<>());

        logger.info("Состояние пользователя {} сброшено: {}", user.getUsername(), user.getState());


        
        // Отправляем изображение start.jpg (временно отключено)
        // String imagePath = "https://i.ibb.co/67WZjKj6/start.jpg";
        // sendPhoto(user.getChatId(), imagePath, "Начнем подбор БАДов для вас!");

        // Отправляем первый вопрос
        sendNextQuestion(user);
    }

    private void handleHelpCommand(TelegramUser user) {
        String helpMessage = "*Доступные команды:*\n\n" +
                "• `/start` - Начать опрос заново\n" +
                "• `/help` - Показать эту справку\n" +
                "• `/reset` - Сбросить текущий опрос\n\n" +
                "Если у вас возникли проблемы, попробуйте команду `/reset`";

        sendMessage(user.getChatId(), helpMessage);
    }

    private void handleResetCommand(TelegramUser user) {
        logger.info("Выполнение команды /reset для пользователя {}", user.getUsername());

        // Полностью сбрасываем состояние пользователя
        user.resetSurvey();
        user.setState(TelegramUser.UserState.SURVEY_IN_PROGRESS);
        user.setSurveyCompleted(false);
        user.setCurrentQuestionIndex(0);
        user.setSelectedTopic(null);
        user.setAnswers(new HashMap<>());

        logger.info("Состояние пользователя {} сброшено командой /reset: {}", user.getUsername(), user.getState());

        // Отправляем сообщение о сбросе
        String resetMessage = "🔄 *Опрос сброшен!*\n\n" +
                "Давайте начнем заново с выбора темы здоровья.";

        sendMessage(user.getChatId(), resetMessage);

        // Отправляем первый вопрос
        sendNextQuestion(user);
    }

    private void handleGeneticsCommand(TelegramUser user) {
        logger.info("Пользователь {} заинтересовался генетическим тестированием", user.getUsername());

        String geneticsMessage = "🧬 *GenAIS™ — Персональная генетика для точных рекомендаций*\n\n" +
                "Наша система анализирует ваши гены и создает индивидуальную схему приема БАДов:\n\n" +
                "• 🔍 Проверяет риски по генам\n" +
                "• 📊 Анализирует метаболизм\n" +
                "• 💊 Подбирает оптимальные дозировки\n" +
                "• ⚠️ Выявляет противопоказания\n\n" +
                "Получите максимально точные рекомендации на основе вашей ДНК!\n\n" +
                "🌐 Переходите на сайт: https://soloways.tilda.ws/pers_bad";

        sendMessage(user.getChatId(), geneticsMessage);
    }

    private void handleRegularMessage(TelegramUser user, String messageText) {
        switch (user.getState()) {
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

    private void handleCompletedSurvey(TelegramUser user, String message) {
        if ("/start".equals(message)) {
            handleStartCommand(user);
        } else if ("/help".equals(message)) {
            handleHelpCommand(user);
        } else {
            sendMessage(user.getChatId(), "Опрос уже завершен. Используйте команду /start для начала нового опроса или /help для справки.");
        }
    }



    private void handleSurveyAnswer(TelegramUser user, String answer) {
        logger.info("Обработка ответа пользователя {}: '{}', текущий индекс={}", 
                   user.getUsername(), answer, user.getCurrentQuestionIndex());
        
        // Обрабатываем ответ через сервис опроса
        surveyService.processAnswer(user, answer);
        
        logger.info("После обработки ответа: индекс={}, завершен={}", 
                   user.getCurrentQuestionIndex(), surveyService.isSurveyCompleted(user));
        
        // Проверяем, завершен ли опрос
        if (surveyService.isSurveyCompleted(user)) {
            logger.info("Опрос завершен для пользователя {}, показываем рекомендации", user.getUsername());
            completeSurvey(user);
        } else {
            sendNextQuestion(user);
        }
    }



    private void sendNextQuestion(TelegramUser user) {
        logger.info("=== НАЧАЛО ОТПРАВКИ СЛЕДУЮЩЕГО ВОПРОСА ===");
        logger.info("Получаем следующий вопрос для пользователя: {}", user.getUsername());
        
        TelegramSurveyService.SurveyQuestion question = surveyService.getNextQuestion(user);
        
        logger.info("Отправка вопроса для пользователя {}: текущий индекс={}, вопрос={}", 
                   user.getUsername(), user.getCurrentQuestionIndex(), 
                   question != null ? question.getText() : "null");
        
        if (question == null) {
            logger.info("Опрос завершен для пользователя {}, показываем рекомендации", user.getUsername());
            completeSurvey(user);
            return;
        }
        
        String selectedTopic = user.getSelectedTopic();
        int totalQuestions = selectedTopic != null ? surveyService.getTotalQuestionsForTopic(selectedTopic) : 1;
        int currentQuestion = user.getCurrentQuestionIndex() + 1;
        
        logger.info("Тема: {}, Всего вопросов: {}, Текущий вопрос: {}", selectedTopic, totalQuestions, currentQuestion);
        
        // Для первого вопроса (выбор темы) убираем нумерацию
        String questionText;
        if (user.getCurrentQuestionIndex() == 0) {
            questionText = question.getText();
        } else {
            questionText = "Вопрос " + currentQuestion + " из " + totalQuestions + ":\n\n" + question.getText();
        }
        logger.info("Текст вопроса: {}", questionText);

        logger.info("Создаем inline клавиатуру с {} вариантами ответов", question.getOptions().size());
        InlineKeyboardMarkup keyboard = createInlineAnswerKeyboard(question.getOptions().toArray(new String[0]));

        logger.info("Отправляем сообщение с inline клавиатурой в чат: {}", user.getChatId());
        sendMessageWithInlineKeyboard(user.getChatId(), questionText, keyboard);
        
        logger.info("=== КОНЕЦ ОТПРАВКИ СЛЕДУЮЩЕГО ВОПРОСА ===");
    }

    private void completeSurvey(TelegramUser user) {
        user.setState(TelegramUser.UserState.SURVEY_COMPLETED);
        user.setSurveyCompleted(true);

        // Получаем рекомендации
        try {
            ScoreCalculationService.RecommendationResult result = surveyService.getRecommendations(user);
            
            logger.info("Получены рекомендации для пользователя {}: основные={}, дополнительные={}", 
                user.getUsername(),
                result.getMainRecommendations() != null ? result.getMainRecommendations().size() : 0,
                result.getAdditionalRecommendations() != null ? result.getAdditionalRecommendations().size() : 0
            );
            
            // 1. Отправляем заголовок основных рекомендаций
            String mainRecommendationsHeader = "*Основные рекомендации*\nСовместимы, безопасны, рассчитаны на совместный приём — рекомендуем принимать курсом 3 месяца";
            sendMessage(user.getChatId(), mainRecommendationsHeader);
            
            // Небольшая задержка для правильного порядка сообщений
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 2. Отправляем альбом из 3 фото основных БАДов
            if (result.getMainRecommendations() != null && !result.getMainRecommendations().isEmpty()) {
                logger.info("Отправляем {} основных рекомендаций", result.getMainRecommendations().size());
                for (int i = 0; i < Math.min(3, result.getMainRecommendations().size()); i++) {
                    ScoreCalculationService.SupplementWithScore supplementWithScore = result.getMainRecommendations().get(i);
                    Supplement supplement = supplementWithScore.getSupplement();
                    
                    logger.info("Обрабатываем основную рекомендацию {}: {}", i, supplement.getName());
                    logger.info("ImageUrl: {}, ProductUrl: {}", supplement.getImageUrl(), supplement.getProductUrl());
                    
                    // Формируем подпись с Markdown
                    String caption = String.format("*%s*\n\n%s\n\n*Баллы:* %s", 
                        supplement.getName(),
                        supplement.getDescription() != null ? supplement.getDescription() : "Описание отсутствует",
                        supplementWithScore.getScore()
                    );
                    
                    // Отправляем фото с inline кнопкой "Подробнее"
                    String buttonUrl = supplement.getProductUrl() != null ? supplement.getProductUrl() : "https://soloways.tilda.ws";
                    if (supplement.getImageUrl() != null && !supplement.getImageUrl().isEmpty()) {
                        logger.info("Отправляем фото для {}", supplement.getName());
                        sendPhotoWithInlineButton(user.getChatId(), supplement.getImageUrl(), caption, "Подробнее", buttonUrl);
                    } else {
                        logger.info("Отправляем текст без фото для {}", supplement.getName());
                        // Если нет фото, отправляем только текст с кнопкой
                        sendMessageWithInlineKeyboard(user.getChatId(), caption, 
                            createInlineButtonKeyboard("Подробнее", buttonUrl));
                    }
                }
            } else {
                logger.info("Основные рекомендации отсутствуют");
            }
            
            // 3. Отправляем заголовок дополнительных рекомендаций
            String additionalRecommendationsHeader = "*Дополнительные рекомендации*\nЭти добавки безопасно сочетаются с основными и усиливают их действие: можете подключать их вместе или позже, рекомендуемый курс — 3 месяца";
            sendMessage(user.getChatId(), additionalRecommendationsHeader);
            
            // Небольшая задержка для правильного порядка сообщений
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 4. Отправляем альбом из 2 фото дополнительных БАДов
            if (result.getAdditionalRecommendations() != null && !result.getAdditionalRecommendations().isEmpty()) {
                logger.info("Отправляем {} дополнительных рекомендаций", result.getAdditionalRecommendations().size());
                for (int i = 0; i < Math.min(2, result.getAdditionalRecommendations().size()); i++) {
                    ScoreCalculationService.SupplementWithScore supplementWithScore = result.getAdditionalRecommendations().get(i);
                    Supplement supplement = supplementWithScore.getSupplement();
                    
                    logger.info("Обрабатываем дополнительную рекомендацию {}: {}", i, supplement.getName());
                    logger.info("ImageUrl: {}, ProductUrl: {}", supplement.getImageUrl(), supplement.getProductUrl());
                    
                    // Формируем подпись с Markdown
                    String caption = String.format("*%s*\n\n%s\n\n*Баллы:* %s", 
                        supplement.getName(),
                        supplement.getDescription() != null ? supplement.getDescription() : "Описание отсутствует",
                        supplementWithScore.getScore()
                    );
                    
                    // Отправляем фото с inline кнопкой "Подробнее"
                    String buttonUrl = supplement.getProductUrl() != null ? supplement.getProductUrl() : "https://soloways.tilda.ws";
                    if (supplement.getImageUrl() != null && !supplement.getImageUrl().isEmpty()) {
                        logger.info("Отправляем фото для {}", supplement.getName());
                        sendPhotoWithInlineButton(user.getChatId(), supplement.getImageUrl(), caption, "Подробнее", buttonUrl);
                    } else {
                        logger.info("Отправляем текст без фото для {}", supplement.getName());
                        // Если нет фото, отправляем только текст с кнопкой
                        sendMessageWithInlineKeyboard(user.getChatId(), caption, 
                            createInlineButtonKeyboard("Подробнее", buttonUrl));
                    }
                }
            } else {
                logger.info("Дополнительные рекомендации отсутствуют");
            }
            
            // 5. Отправляем финальное сообщение с кнопками
            String finalMessage = "💡 *Совет:* Проконсультируйтесь с врачом перед приемом любых добавок.\n\n" +
                                "🔄 Хотите пройти опрос заново?\n\n" +
                                "Хотите ещё точнее? GenAIS™ проверит риски по генам и обновит схему";
            
            // Создаем клавиатуру с кнопками
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
            
            // Первый ряд: кнопка нового опроса
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton startButton = new InlineKeyboardButton();
            startButton.setText("🔄 Начать новый опрос");
            startButton.setCallbackData("NEW_SURVEY");
            row1.add(startButton);
            keyboardRows.add(row1);
            
            // Второй ряд: кнопка генетики
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton geneticsButton = new InlineKeyboardButton();
            geneticsButton.setText("🧬 Хочу точнее");
            geneticsButton.setCallbackData("GENETICS");
            row2.add(geneticsButton);
            keyboardRows.add(row2);
            
            keyboard.setKeyboard(keyboardRows);
            
            sendMessageWithKeyboard(user.getChatId(), finalMessage, keyboard);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении рекомендаций для пользователя {}: {}", user.getUsername(), e.getMessage(), e);
            
            String errorMessage = "К сожалению, произошла ошибка при формировании рекомендаций. Попробуйте пройти опрос заново, отправив /start";
            sendMessage(user.getChatId(), errorMessage);
        }
    }



    private InlineKeyboardMarkup createInlineAnswerKeyboard(String... options) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        
        // Размещаем все кнопки по одной в ряду (друг под другом)
        for (String option : options) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(option);
            button.setCallbackData(option);
            row.add(button);
            keyboardRows.add(row);
        }

        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }
    
    private InlineKeyboardMarkup createInlineButtonKeyboard(String buttonText, String buttonUrl) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setUrl(buttonUrl);
        row.add(button);
        keyboardRows.add(row);
        
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }

    private ReplyKeyboardMarkup createAnswerKeyboard(String... options) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        keyboard.setSelective(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        
        // Для вопросов с выбором темы - размещаем по 1 кнопке в ряду
        if (options.length > 10) {
            for (String option : options) {
                KeyboardRow row = new KeyboardRow();
                KeyboardButton button = new KeyboardButton();
                button.setText(option);
                row.add(button);
                keyboardRows.add(row);
            }
        } else {
            // Для обычных вопросов - размещаем по 3 кнопки в ряду (как на скриншоте)
            KeyboardRow row = new KeyboardRow();
            for (String option : options) {
                KeyboardButton button = new KeyboardButton();
                button.setText(option);
                row.add(button);
                if (row.size() == 3) {
                    keyboardRows.add(row);
                    row = new KeyboardRow();
                }
            }
            if (!row.isEmpty()) {
                keyboardRows.add(row);
            }
        }

        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }



    private void answerCallbackQuery(String callbackData) {
        try {
            String url = "https://api.telegram.org/bot" + botConfig.getBotToken() + "/answerCallbackQuery";
            String jsonBody = String.format(
                "{\"callback_query_id\":\"%s\"}",
                callbackData
            );

            logger.info("Отвечаем на callback query: {}", callbackData);

            webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        response -> logger.info("✅ Ответ на callback query отправлен: {}", response),
                        error -> logger.error("❌ Ошибка ответа на callback query: {}", error.getMessage())
                    );

        } catch (Exception e) {
            logger.error("Error answering callback query: {}", e.getMessage(), e);
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            String url = "https://api.telegram.org/bot" + botConfig.getBotToken() + "/sendMessage";
            String jsonBody = String.format(
                "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\"}",
                chatId, text.replace("\"", "\\\"").replace("\n", "\\n")
            );

            logger.info("Отправка сообщения в чат {}: {}", chatId, text);
            logger.info("URL: {}", url);
            logger.info("JSON: {}", jsonBody);

            webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        response -> logger.info("✅ Сообщение отправлено в чат {}: {}", chatId, response),
                        error -> logger.error("❌ Ошибка отправки сообщения в чат {}: {}", chatId, error.getMessage())
                    );

        } catch (Exception e) {
            logger.error("Error sending message to {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void sendPhoto(Long chatId, String imagePath, String caption) {
        try {
            String url = "https://api.telegram.org/bot" + botConfig.getBotToken() + "/sendPhoto";
            
            String jsonBody = String.format(
                "{\"chat_id\":\"%s\",\"photo\":\"%s\",\"caption\":\"%s\"}",
                chatId, imagePath, caption.replace("\"", "\\\"")
            );

            logger.info("Отправка фотографии в чат {}: {}", chatId, caption);
            logger.info("URL: {}", url);
            logger.info("JSON: {}", jsonBody);

            webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        response -> logger.info("✅ Фотография отправлена в чат {}: {}", chatId, response),
                        error -> logger.error("❌ Ошибка отправки фотографии в чат {}: {}", chatId, error.getMessage())
                    );

        } catch (Exception e) {
            logger.error("Error sending photo to {}: {}", chatId, e.getMessage(), e);
        }
    }
    
    private void sendPhotoWithInlineButton(Long chatId, String imagePath, String caption, String buttonText, String buttonUrl) {
        try {
            String url = "https://api.telegram.org/bot" + botConfig.getBotToken() + "/sendPhoto";
            
            // Создаем inline кнопку
            String keyboardJson = String.format(
                "\"reply_markup\":{\"inline_keyboard\":[[{\"text\":\"%s\",\"url\":\"%s\"}]]}",
                buttonText, buttonUrl
            );
            
            String jsonBody = String.format(
                "{\"chat_id\":\"%s\",\"photo\":\"%s\",\"caption\":\"%s\",\"parse_mode\":\"Markdown\",%s}",
                chatId, imagePath, caption.replace("\"", "\\\""), keyboardJson
            );

            logger.info("Отправка фотографии с inline кнопкой в чат {}: {}", chatId, caption);
            logger.info("URL: {}", url);
            logger.info("JSON: {}", jsonBody);

            webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        response -> logger.info("✅ Фотография с кнопкой отправлена в чат {}: {}", chatId, response),
                        error -> logger.error("❌ Ошибка отправки фотографии с кнопкой в чат {}: {}", chatId, error.getMessage())
                    );

        } catch (Exception e) {
            logger.error("Error sending photo with inline button to {}: {}", chatId, e.getMessage(), e);
        }
    }
    


    private void sendMessageWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            logger.info("=== НАЧАЛО ОТПРАВКИ СООБЩЕНИЯ С INLINE КЛАВИАТУРОЙ ===");
            String url = "https://api.telegram.org/bot" + botConfig.getBotToken() + "/sendMessage";
            
            // Создаем JSON для inline клавиатуры
            StringBuilder keyboardJson = new StringBuilder();
            keyboardJson.append("\"reply_markup\":{");
            keyboardJson.append("\"inline_keyboard\":[");
            
            List<List<InlineKeyboardButton>> keyboardButtons = keyboard.getKeyboard();
            logger.info("Количество рядов кнопок: {}", keyboardButtons.size());
            
            // Добавляем кнопки в JSON
            for (int i = 0; i < keyboardButtons.size(); i++) {
                keyboardJson.append("[");
                List<InlineKeyboardButton> row = keyboardButtons.get(i);
                logger.info("Ряд {}: {} кнопок", i, row.size());
                for (int j = 0; j < row.size(); j++) {
                    InlineKeyboardButton button = row.get(j);
                    logger.info("Кнопка {}: текст='{}', callback_data='{}'", j, button.getText(), button.getCallbackData());
                    keyboardJson.append("{");
                    keyboardJson.append("\"text\":\"").append(button.getText().replace("\"", "\\\"")).append("\",");
                    keyboardJson.append("\"callback_data\":\"").append(button.getCallbackData().replace("\"", "\\\"")).append("\"");
                    keyboardJson.append("}");
                    if (j < row.size() - 1) {
                        keyboardJson.append(",");
                    }
                }
                keyboardJson.append("]");
                if (i < keyboardButtons.size() - 1) {
                    keyboardJson.append(",");
                }
            }
            
            keyboardJson.append("]}");
            
            String jsonBody = String.format(
                "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\",%s}",
                chatId, 
                text.replace("\"", "\\\"").replace("\n", "\\n"),
                keyboardJson.toString()
            );

            logger.info("Отправка сообщения с inline клавиатурой в чат {}: {}", chatId, text);
            logger.info("URL: {}", url);
            logger.info("JSON: {}", jsonBody);

            webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        response -> {
                            logger.info("✅ Сообщение с inline клавиатурой отправлено в чат {}: {}", chatId, response);
                            logger.info("=== КОНЕЦ ОТПРАВКИ СООБЩЕНИЯ С INLINE КЛАВИАТУРОЙ ===");
                        },
                        error -> {
                            logger.error("❌ Ошибка отправки сообщения с inline клавиатурой в чат {}: {}", chatId, error.getMessage());
                            logger.info("=== КОНЕЦ ОТПРАВКИ СООБЩЕНИЯ С INLINE КЛАВИАТУРОЙ (ОШИБКА) ===");
                        }
                    );

        } catch (Exception e) {
            logger.error("Error sending message with inline keyboard to {}: {}", chatId, e.getMessage(), e);
            logger.info("=== КОНЕЦ ОТПРАВКИ СООБЩЕНИЯ С INLINE КЛАВИАТУРОЙ (ИСКЛЮЧЕНИЕ) ===");
        }
    }



    private void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            String url = "https://api.telegram.org/bot" + botConfig.getBotToken() + "/sendMessage";
            
            // Создаем JSON для inline клавиатуры
            StringBuilder keyboardJson = new StringBuilder();
            keyboardJson.append("\"reply_markup\":{");
            keyboardJson.append("\"inline_keyboard\":[");
            
            List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
            
            for (int i = 0; i < rows.size(); i++) {
                List<InlineKeyboardButton> row = rows.get(i);
                keyboardJson.append("[");
                
                for (int j = 0; j < row.size(); j++) {
                    InlineKeyboardButton button = row.get(j);
                    keyboardJson.append("{");
                    keyboardJson.append("\"text\":\"").append(button.getText().replace("\"", "\\\"")).append("\",");
                    keyboardJson.append("\"callback_data\":\"").append(button.getCallbackData().replace("\"", "\\\"")).append("\"");
                    keyboardJson.append("}");
                    
                    if (j < row.size() - 1) {
                        keyboardJson.append(",");
                    }
                }
                
                keyboardJson.append("]");
                if (i < rows.size() - 1) {
                    keyboardJson.append(",");
                }
            }
            
            keyboardJson.append("]}");
            
            String jsonBody = String.format(
                "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\",%s}",
                chatId, 
                text.replace("\"", "\\\"").replace("\n", "\\n"),
                keyboardJson.toString()
            );

            logger.info("Отправка сообщения с inline клавиатурой в чат {}: {}", chatId, text);
            logger.info("URL: {}", url);
            logger.info("JSON: {}", jsonBody);

            webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        response -> logger.info("✅ Сообщение с inline клавиатурой отправлено в чат {}: {}", chatId, response),
                        error -> logger.error("❌ Ошибка отправки сообщения с inline клавиатурой в чат {}: {}", chatId, error.getMessage())
                    );

        } catch (Exception e) {
            logger.error("Error sending message with inline keyboard to {}: {}", chatId, e.getMessage(), e);
        }
    }
}
