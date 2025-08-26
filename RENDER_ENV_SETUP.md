# Настройка переменных окружения на Render.com

## Шаг 1: Создание нового Web Service

1. Войдите в [Render.com](https://render.com)
2. Нажмите "New +" → "Web Service"
3. Подключите ваш GitHub репозиторий: `https://github.com/Sikorsky28/BadRecommender-.git`

## Шаг 2: Настройка переменных окружения

В разделе "Environment Variables" добавьте следующие переменные:

### Основные настройки
```
SPRING_PROFILES_ACTIVE = production
PORT = 8080
```

### Google Sheets настройки
```
GOOGLE_SHEETS_SPREADSHEET_ID = 1xoz1hpg9XIcci4j9YByJiWQ2PYuiMb2cOX_Nu03rblM
GOOGLE_CREDENTIALS_FILE = google-credentials.json
```

### Email настройки (MailoPost)
```
MAIL_HOST = smtp.msndr.net
MAIL_PORT = 465
MAIL_USERNAME = marketing@soloways.ru
MAIL_PASSWORD = e016348c8debb844652c586076fb1c8f
```

### CORS настройки
```
CORS_ALLOWED_ORIGINS = https://*.tilda.ws,https://*.tilda.site,https://*.tilda.com,https://your-tilda-site.com,https://www.your-tilda-site.com
```

### Кэш настройки
```
CACHE_TTL = 300000
CACHE_MAX_SIZE = 1000
```

## Шаг 3: Настройка Build & Deploy

### Build Command
```
./gradlew clean build -x test
```

### Start Command
```
java -jar build/libs/BadRecommender-0.0.1-SNAPSHOT.jar
```

## Шаг 4: Дополнительные настройки

### Region
Выберите ближайший к вашим пользователям регион (например, `Oregon`)

### Plan
Для начала используйте `Free` план

### Environment
Выберите `Java`

## Шаг 5: Проверка деплоя

После настройки всех переменных:

1. Нажмите "Create Web Service"
2. Дождитесь завершения сборки и деплоя
3. Проверьте логи на наличие ошибок
4. Протестируйте API endpoints

## Важные замечания

### Безопасность
- Все пароли и ключи уже зашифрованы в переменных окружения
- Файл `google-credentials.json` должен быть в корне репозитория
- Не добавляйте чувствительные данные в код

### CORS
- В продакшене замените `your-tilda-site.com` на реальный домен вашего Tilda сайта
- Убедитесь, что домены указаны правильно

### Мониторинг
- Настройте уведомления о сбоях
- Регулярно проверяйте логи приложения

## Возможные проблемы

### Ошибка с Google Sheets
Если возникает ошибка "Unexpected exception reading PKCS data":
1. Проверьте, что файл `google-credentials.json` корректный
2. Убедитесь, что переменная `GOOGLE_CREDENTIALS_FILE` установлена правильно

### Ошибка с Email
Если email не отправляется:
1. Проверьте настройки SMTP
2. Убедитесь, что порт 465 открыт
3. Проверьте логи на наличие ошибок аутентификации

### Проблемы с CORS
Если возникают CORS ошибки:
1. Проверьте настройку `CORS_ALLOWED_ORIGINS`
2. Убедитесь, что домены указаны без протокола (https://)
3. Добавьте ваш реальный домен в список разрешенных

## Полезные команды для отладки

### Проверка переменных окружения
```bash
echo $SPRING_PROFILES_ACTIVE
echo $GOOGLE_SHEETS_SPREADSHEET_ID
echo $MAIL_HOST
```

### Проверка файлов
```bash
ls -la google-credentials.json
cat google-credentials.json | head -5
```

### Проверка портов
```bash
netstat -tlnp | grep :8080
```
