# Скрипт для настройки Telegram Webhook
# Запустите этот скрипт после получения токена бота

param(
    [Parameter(Mandatory=$true)]
    [string]$BotToken,
    
    [Parameter(Mandatory=$false)]
    [string]$AppUrl = "https://badrecommender.onrender.com"
)

Write-Host "🤖 Настройка Telegram Webhook" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

# Проверяем токен бота
Write-Host "🔍 Проверяем токен бота..." -ForegroundColor Yellow
$botInfoUrl = "https://api.telegram.org/bot$BotToken/getMe"
try {
    $botInfo = Invoke-RestMethod -Uri $botInfoUrl -Method GET
    Write-Host "✅ Бот найден: @$($botInfo.result.username)" -ForegroundColor Green
    Write-Host "   Имя: $($botInfo.result.first_name)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Ошибка при проверке токена бота: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Удаляем старый webhook (если есть)
Write-Host "🗑️ Удаляем старый webhook..." -ForegroundColor Yellow
try {
    $deleteUrl = "https://api.telegram.org/bot$BotToken/deleteWebhook"
    $deleteResult = Invoke-RestMethod -Uri $deleteUrl -Method POST
    if ($deleteResult.ok) {
        Write-Host "✅ Старый webhook удален" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️ Не удалось удалить старый webhook: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Настраиваем новый webhook
Write-Host "🔗 Настраиваем новый webhook..." -ForegroundColor Yellow
$webhookUrl = "$AppUrl/webhook/$BotToken"
$webhookData = @{
    url = $webhookUrl
    allowed_updates = @("message", "callback_query")
    drop_pending_updates = $true
} | ConvertTo-Json

try {
    $setWebhookUrl = "https://api.telegram.org/bot$BotToken/setWebhook"
    $result = Invoke-RestMethod -Uri $setWebhookUrl -Method POST -Body $webhookData -ContentType "application/json"
    
    if ($result.ok) {
        Write-Host "✅ Webhook успешно настроен!" -ForegroundColor Green
        Write-Host "   URL: $webhookUrl" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Ошибка при настройке webhook: $($result.description)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Ошибка при настройке webhook: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Проверяем статус webhook
Write-Host "🔍 Проверяем статус webhook..." -ForegroundColor Yellow
try {
    $webhookInfoUrl = "https://api.telegram.org/bot$BotToken/getWebhookInfo"
    $webhookInfo = Invoke-RestMethod -Uri $webhookInfoUrl -Method GET
    
    if ($webhookInfo.ok) {
        Write-Host "✅ Webhook активен" -ForegroundColor Green
        Write-Host "   URL: $($webhookInfo.result.url)" -ForegroundColor Cyan
        Write-Host "   Ошибок: $($webhookInfo.result.last_error_message)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ Ошибка при проверке статуса webhook: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🎉 Настройка завершена!" -ForegroundColor Green
Write-Host "Теперь найдите вашего бота в Telegram и отправьте команду /start" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Переменные окружения для Render.com:" -ForegroundColor Yellow
Write-Host "TELEGRAM_BOT_ENABLED=true" -ForegroundColor White
Write-Host "TELEGRAM_BOT_TOKEN=$BotToken" -ForegroundColor White
Write-Host "TELEGRAM_BOT_USERNAME=$($botInfo.result.username)" -ForegroundColor White
