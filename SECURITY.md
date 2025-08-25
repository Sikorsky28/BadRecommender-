# 🔐 Безопасность приложения

## Переменные окружения

### Обязательные переменные для production:

```bash
# Email настройки (MailoPost)
MAIL_HOST=smtp.msndr.net
MAIL_PORT=465
MAIL_USERNAME=marketing@soloways.ru
MAIL_PASSWORD=your_secure_password_here

# Google Sheets API
GOOGLE_SHEETS_SPREADSHEET_ID=1xoz1hpg9XIcci4j9YByJiWQ2PYuiMb2cOX_Nu03rblM
GOOGLE_CREDENTIALS_FILE=google-credentials.json

# Настройки приложения
PORT=8080
SPRING_PROFILES_ACTIVE=production
CORS_ALLOWED_ORIGINS=*
```

## Настройка в Render.com

1. Перейдите в Dashboard вашего сервиса
2. Вкладка **Environment**
3. Добавьте переменные:
   - `MAIL_USERNAME` = `marketing@soloways.ru`
   - `MAIL_PASSWORD` = `ваш_пароль_от_mailopost`
   - `GOOGLE_CREDENTIALS_FILE` = загрузите файл в **Secret Files**

## Локальная разработка

1. Создайте файл `.env` в корне проекта
2. Скопируйте переменные из примера выше
3. Заполните реальными значениями

## Важно!

- ❌ **НЕ коммитьте** файлы с паролями в Git
- ❌ **НЕ публикуйте** секреты в открытом доступе
- ✅ **Используйте** переменные окружения
- ✅ **Регулярно меняйте** пароли
- ✅ **Используйте** Secret Files в Render.com
