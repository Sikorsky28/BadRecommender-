# Быстрая настройка Render.com

## Переменные окружения для копирования

```
SPRING_PROFILES_ACTIVE=production
PORT=8080
GOOGLE_SHEETS_SPREADSHEET_ID=1xoz1hpg9XIcci4j9YByJiWQ2PYuiMb2cOX_Nu03rblM
GOOGLE_CREDENTIALS_FILE=google-credentials.json
MAIL_HOST=smtp.msndr.net
MAIL_PORT=465
MAIL_USERNAME=marketing@soloways.ru
MAIL_PASSWORD=e016348c8debb844652c586076fb1c8f
CORS_ALLOWED_ORIGINS=https://*.tilda.ws,https://*.tilda.site,https://*.tilda.com,https://your-tilda-site.com,https://www.your-tilda-site.com
CACHE_TTL=300000
CACHE_MAX_SIZE=1000
TELEGRAM_BOT_ENABLED=false
TELEGRAM_BOT_TOKEN=
TELEGRAM_BOT_USERNAME=
```

## Команды сборки и запуска

**Build Command:**
```
./gradlew clean build -x test
```

**Start Command:**
```
java -jar build/libs/BadRecommender-0.0.1-SNAPSHOT.jar
```

## Настройки сервиса

- **Environment:** Java
- **Region:** Oregon (или ближайший к пользователям)
- **Plan:** Free
- **Repository:** https://github.com/Sikorsky28/BadRecommender-.git

## Проверка после деплоя

1. Откройте URL вашего сервиса
2. Добавьте `/actuator/health` для проверки здоровья
3. Проверьте логи на наличие ошибок
4. Протестируйте API endpoints

## Настройка Telegram бота (опционально)

1. Создайте бота через @BotFather в Telegram
2. Получите токен и username
3. Установите переменные:
   ```
   TELEGRAM_BOT_ENABLED=true
   TELEGRAM_BOT_TOKEN=ваш_токен
   TELEGRAM_BOT_USERNAME=ваш_username
   ```
4. Протестируйте бота командой `/start`

## Важно!

- Файл `google-credentials.json` должен быть в репозитории
- Замените `your-tilda-site.com` на реальный домен
- Все пароли уже настроены и безопасны
- Telegram бот отключен по умолчанию
