#!/bin/bash

# Скрипт для настройки Telegram Webhook
# Запустите этот скрипт после получения токена бота

# Проверяем, что токен передан
if [ -z "$1" ]; then
    echo "❌ Ошибка: Не указан токен бота"
    echo "Использование: ./setup-webhook.sh YOUR_BOT_TOKEN"
    exit 1
fi

BOT_TOKEN=$1
APP_URL=${2:-"https://badrecommender.onrender.com"}

echo "🤖 Настройка Telegram Webhook"
echo "================================"

# Проверяем токен бота
echo "🔍 Проверяем токен бота..."
BOT_INFO=$(curl -s "https://api.telegram.org/bot$BOT_TOKEN/getMe")

if echo "$BOT_INFO" | grep -q '"ok":true'; then
    BOT_USERNAME=$(echo "$BOT_INFO" | grep -o '"username":"[^"]*"' | cut -d'"' -f4)
    BOT_NAME=$(echo "$BOT_INFO" | grep -o '"first_name":"[^"]*"' | cut -d'"' -f4)
    echo "✅ Бот найден: @$BOT_USERNAME"
    echo "   Имя: $BOT_NAME"
else
    echo "❌ Ошибка при проверке токена бота"
    exit 1
fi

# Удаляем старый webhook (если есть)
echo "🗑️ Удаляем старый webhook..."
DELETE_RESULT=$(curl -s -X POST "https://api.telegram.org/bot$BOT_TOKEN/deleteWebhook")
if echo "$DELETE_RESULT" | grep -q '"ok":true'; then
    echo "✅ Старый webhook удален"
fi

# Настраиваем новый webhook
echo "🔗 Настраиваем новый webhook..."
WEBHOOK_URL="$APP_URL/webhook/$BOT_TOKEN"

WEBHOOK_DATA='{
    "url": "'$WEBHOOK_URL'",
    "allowed_updates": ["message"],
    "drop_pending_updates": true
}'

SET_RESULT=$(curl -s -X POST "https://api.telegram.org/bot$BOT_TOKEN/setWebhook" \
    -H "Content-Type: application/json" \
    -d "$WEBHOOK_DATA")

if echo "$SET_RESULT" | grep -q '"ok":true'; then
    echo "✅ Webhook успешно настроен!"
    echo "   URL: $WEBHOOK_URL"
else
    echo "❌ Ошибка при настройке webhook"
    echo "$SET_RESULT"
    exit 1
fi

# Проверяем статус webhook
echo "🔍 Проверяем статус webhook..."
WEBHOOK_INFO=$(curl -s "https://api.telegram.org/bot$BOT_TOKEN/getWebhookInfo")

if echo "$WEBHOOK_INFO" | grep -q '"ok":true'; then
    echo "✅ Webhook активен"
    WEBHOOK_URL_INFO=$(echo "$WEBHOOK_INFO" | grep -o '"url":"[^"]*"' | cut -d'"' -f4)
    echo "   URL: $WEBHOOK_URL_INFO"
    ERROR_MSG=$(echo "$WEBHOOK_INFO" | grep -o '"last_error_message":"[^"]*"' | cut -d'"' -f4)
    if [ ! -z "$ERROR_MSG" ]; then
        echo "   Ошибок: $ERROR_MSG"
    fi
fi

echo ""
echo "🎉 Настройка завершена!"
echo "Теперь найдите вашего бота в Telegram и отправьте команду /start"
echo ""
echo "📋 Переменные окружения для Render.com:"
echo "TELEGRAM_BOT_ENABLED=true"
echo "TELEGRAM_BOT_TOKEN=$BOT_TOKEN"
echo "TELEGRAM_BOT_USERNAME=$BOT_USERNAME"
