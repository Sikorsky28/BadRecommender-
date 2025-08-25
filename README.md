# 🏥 SOLOWAYS - Система персональных рекомендаций БАДов

## 📋 Описание проекта

Веб-приложение для персонализированных рекомендаций биологически активных добавок (БАД) на основе ответов пользователей на вопросы о здоровье и образе жизни.

### 🎯 Основные функции

- **Персонализированный опрос** - 15 вопросов для определения потребностей
- **Умная система рекомендаций** - алгоритм подбора БАДов на основе ответов
- **Email рассылка** - автоматическая отправка персональных рекомендаций
- **Интеграция с Google Sheets** - динамическое обновление данных о БАДах
- **Адаптивный дизайн** - работает на всех устройствах

## 🚀 Быстрый старт

### Требования
- Java 21+
- Gradle 8.0+

### Локальный запуск

1. **Клонируйте репозиторий:**
```bash
git clone https://github.com/your-username/BadRecommender.git
cd BadRecommender
```

2. **Настройте переменные окружения:**
Создайте файл `.env` в корне проекта:
```bash
MAIL_HOST=smtp.msndr.net
MAIL_PORT=465
MAIL_USERNAME=your_email@domain.com
MAIL_PASSWORD=your_password
GOOGLE_SHEETS_SPREADSHEET_ID=your_spreadsheet_id
GOOGLE_CREDENTIALS_FILE=google-credentials.json
```

3. **Запустите приложение:**
```bash
./gradlew bootRun
```

4. **Откройте в браузере:**
```
http://localhost:8080
```

## 📁 Структура проекта

```
BadRecommender/
├── src/main/java/com/soloway/BadRecommender/
│   ├── controller/          # REST API контроллеры
│   ├── model/              # Модели данных
│   ├── service/            # Бизнес-логика
│   └── BadRecommenderApplication.java
├── src/main/resources/
│   ├── static/
│   │   └── index.html      # Главная страница
│   └── application.yaml    # Конфигурация
├── build.gradle           # Зависимости
├── Dockerfile            # Docker конфигурация
└── render.yaml           # Render.com конфигурация
```

## 🔧 Конфигурация

### Переменные окружения

| Переменная | Описание | Пример |
|------------|----------|---------|
| `MAIL_HOST` | SMTP сервер | `smtp.msndr.net` |
| `MAIL_PORT` | SMTP порт | `465` |
| `MAIL_USERNAME` | Email для отправки | `marketing@soloways.ru` |
| `MAIL_PASSWORD` | Пароль от email | `your_password` |
| `GOOGLE_SHEETS_SPREADSHEET_ID` | ID таблицы Google Sheets | `1xoz1hpg9XIcci4j9YByJiWQ2PYuiMb2cOX_Nu03rblM` |
| `GOOGLE_CREDENTIALS_FILE` | Файл с ключами Google API | `google-credentials.json` |

## 🌐 Деплой

### Render.com (рекомендуется)

1. Подключите GitHub репозиторий к Render.com
2. Создайте новый Web Service
3. Настройте переменные окружения в Dashboard
4. Загрузите `google-credentials.json` в Secret Files

Подробная инструкция: [DEPLOYMENT-PLAN.md](DEPLOYMENT-PLAN.md)

### Docker

```bash
docker build -t bad-recommender .
docker run -p 8080:8080 bad-recommender
```

## 📧 Email интеграция

Приложение использует MailoPost для отправки email:
- **SMTP сервер:** smtp.msndr.net
- **Порт:** 465 (SSL)
- **Протокол:** SMTP с SSL

## 📊 Google Sheets интеграция

- Автоматическое обновление данных о БАДах
- Динамические цены и описания
- Fallback данные при недоступности API

## 🔐 Безопасность

- Все пароли и ключи хранятся в переменных окружения
- CORS настройки для защиты от несанкционированного доступа
- Валидация входных данных

Подробности: [SECURITY.md](SECURITY.md)

## 🎨 Интеграция с Tilda

Инструкции по интеграции с сайтом на Tilda: [TILDA-INTEGRATION.md](TILDA-INTEGRATION.md)

## 📞 Поддержка

При возникновении вопросов обращайтесь к документации:
- [DEPLOYMENT-PLAN.md](DEPLOYMENT-PLAN.md) - план деплоя
- [SECURITY.md](SECURITY.md) - настройки безопасности
- [TILDA-INTEGRATION.md](TILDA-INTEGRATION.md) - интеграция с Tilda

## 📄 Лицензия

Проект разработан для SOLOWAYS. Все права защищены.
