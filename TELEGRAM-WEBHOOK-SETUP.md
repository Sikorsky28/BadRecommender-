# Настройка Telegram Webhook

## Шаг 1: Получение токена бота

1. Найдите вашего бота в Telegram: @BotFather
2. Отправьте команду `/mybots`
3. Выберите вашего бота
4. Перейдите в "Bot Settings" → "API Token"
5. Скопируйте токен

## Шаг 2: Настройка переменных окружения на Render.com

В настройках вашего приложения на Render.com добавьте следующие переменные окружения:

```
TELEGRAM_BOT_ENABLED=true
TELEGRAM_BOT_TOKEN=YOUR_BOT_TOKEN_HERE
TELEGRAM_BOT_USERNAME=YOUR_BOT_USERNAME
```

## Шаг 3: Настройка Webhook

После деплоя приложения, выполните следующий запрос для настройки webhook:

```bash
curl -X POST "https://api.telegram.org/bot{YOUR_BOT_TOKEN}/setWebhook" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://badrecommender.onrender.com/webhook/{YOUR_BOT_TOKEN}",
    "allowed_updates": ["message"],
    "drop_pending_updates": true
  }'
```

## Шаг 4: Проверка настройки

Проверьте, что webhook настроен правильно:

```bash
curl -X GET "https://api.telegram.org/bot{YOUR_BOT_TOKEN}/getWebhookInfo"
```

## Шаг 5: Тестирование

1. Найдите вашего бота в Telegram
2. Отправьте команду `/start`
3. Пройдите опрос
4. Получите персональные рекомендации

## Полезные команды

### Удаление webhook (если нужно):
```bash
curl -X POST "https://api.telegram.org/bot{YOUR_BOT_TOKEN}/deleteWebhook"
```

### Получение информации о боте:
```bash
curl -X GET "https://api.telegram.org/bot{YOUR_BOT_TOKEN}/getMe"
```

## Структура URL для webhook

Ваш webhook URL будет иметь вид:
```
https://badrecommender.onrender.com/webhook/{YOUR_BOT_TOKEN}
```

Где `{YOUR_BOT_TOKEN}` - это токен вашего бота.

## Логирование

После настройки webhook, все сообщения от пользователей будут приходить на ваш сервер. Логи можно посмотреть в консоли Render.com.

## Безопасность

- Токен бота должен быть секретным
- Webhook URL содержит токен для дополнительной безопасности
- Все запросы проверяются на валидность токена
