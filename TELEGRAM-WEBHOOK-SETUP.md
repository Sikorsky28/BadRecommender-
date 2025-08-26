# Настройка Telegram Webhook

## 🎯 Проблема
Long polling не работает на Render.com из-за ограничений сети. Нужно использовать webhook.

## 🔧 Решение
Мы создали REST контроллер для обработки webhook от Telegram.

## 📋 Шаги настройки

### 1. Получите URL вашего приложения на Render.com
После деплоя получите URL вида: `https://your-app-name.onrender.com`

### 2. Настройте webhook через Telegram API
Отправьте POST запрос к Telegram API:

```bash
curl -X POST "https://api.telegram.org/bot7368002281:AAGVqM_9WOoEAgU8f4tdP_F5rnBK-Lsq460/setWebhook" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://your-app-name.onrender.com/webhook/7368002281:AAGVqM_9WOoEAgU8f4tdP_F5rnBK-Lsq460"
  }'
```

### 3. Проверьте статус webhook
```bash
curl "https://api.telegram.org/bot7368002281:AAGVqM_9WOoEAgU8f4tdP_F5rnBK-Lsq460/getWebhookInfo"
```

### 4. Тестирование
1. Отправьте `/start` боту @SolowaysRecommendationsBot
2. Проверьте логи приложения на Render.com
3. Должно появиться: `=== ПОЛУЧЕНО WEBHOOK ОБНОВЛЕНИЕ ОТ TELEGRAM ===`

## 🔍 Диагностика

### Проверьте логи приложения:
- `✅ Telegram webhook контроллер успешно зарегистрирован в Spring контексте`
- `=== ПОЛУЧЕНО WEBHOOK ОБНОВЛЕНИЕ ОТ TELEGRAM ===`
- `✅ Received webhook message from [username] ([chatId]): /start`

### Если webhook не работает:
1. Проверьте URL в настройке webhook
2. Убедитесь, что приложение доступно по HTTPS
3. Проверьте, что токен бота правильный
4. Проверьте логи на ошибки

## 🚀 Команды для настройки

### Установка webhook (замените YOUR_APP_NAME):
```bash
curl -X POST "https://api.telegram.org/bot7368002281:AAGVqM_9WOoEAgU8f4tdP_F5rnBK-Lsq460/setWebhook" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://YOUR_APP_NAME.onrender.com/webhook/7368002281:AAGVqM_9WOoEAgU8f4tdP_F5rnBK-Lsq460"}'
```

### Проверка статуса:
```bash
curl "https://api.telegram.org/bot7368002281:AAGVqM_9WOoEAgU8f4tdP_F5rnBK-Lsq460/getWebhookInfo"
```

### Удаление webhook (если нужно):
```bash
curl -X POST "https://api.telegram.org/bot7368002281:AAGVqM_9WOoEAgU8f4tdP_F5rnBK-Lsq460/deleteWebhook"
```

## ✅ Ожидаемый результат

После правильной настройки:
1. Бот будет отвечать на команды
2. В логах будут сообщения о получении webhook
3. Сообщения будут отправляться через HTTP API

## 🆘 Если что-то не работает

1. **Проверьте URL приложения** - он должен быть доступен по HTTPS
2. **Проверьте токен бота** - должен совпадать с настройками
3. **Проверьте логи** - ищите ошибки в логах приложения
4. **Проверьте webhook info** - убедитесь, что webhook установлен правильно
