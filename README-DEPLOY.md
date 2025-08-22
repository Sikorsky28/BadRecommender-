# Инструкция по деплою BadRecommender

## Подготовка к деплою

### 1. Файлы, готовые к деплою:
- ✅ JAR файл: `build/libs/BadRecommender-0.0.1-SNAPSHOT.jar`
- ✅ Google Sheets credentials: `google-credentials.json`
- ✅ Production конфигурация: `src/main/resources/application.yaml`
- ✅ Dockerfile для контейнеризации
- ✅ render.yaml для деплоя на Render.com
- ✅ Готовое приложение для интеграции: `src/main/resources/static/index.html`
- ✅ Инструкция по интеграции: `TILDA-INTEGRATION.md`

### 2. Очищенные неиспользуемые файлы:
- ❌ AdminController.java (удален - админка не нужна)
- ❌ DataInitializationController.java (удален - данные уже в Google Sheets)
- ❌ SupplementController.java (удален - CRUD операции не нужны)
- ❌ SupplementService.java (удален - заменен на GoogleSheetsService)
- ❌ TestEmailService.java (удален)
- ❌ widget.html/widget.js (удалены)
- ❌ product-cards.js (удален)
- ❌ Документация .md файлы (удалены)

## Варианты деплоя

### Вариант 1: Render.com (рекомендуется)
1. Загрузите код в GitHub репозиторий
2. Создайте аккаунт на Render.com
3. Подключите репозиторий
4. Используйте `render.yaml` для автоматической настройки

### Вариант 2: Heroku
1. Создайте `Procfile`:
```
web: java -jar build/libs/BadRecommender-0.0.1-SNAPSHOT.jar
```
2. Настройте переменные окружения в Heroku Dashboard

### Вариант 3: Docker
```bash
# Собрать образ
docker build -t bad-recommender .

# Запустить контейнер
docker run -p 8080:8080 \
  -e GOOGLE_SHEETS_SPREADSHEET_ID=your_id \
  -e MAIL_USERNAME=your_email \
  -e MAIL_PASSWORD=your_password \
  bad-recommender
```

## Переменные окружения для production

**Обязательные:**
- `GOOGLE_SHEETS_SPREADSHEET_ID` - ID вашей Google таблицы
- `MAIL_USERNAME` - Email для отправки
- `MAIL_PASSWORD` - Пароль приложения Gmail

**Опциональные:**
- `PORT` - Порт (по умолчанию 8080)
- `CORS_ALLOWED_ORIGINS` - Разрешенные домены для CORS
- `CACHE_TTL` - Время жизни кэша в мс (по умолчанию 300000)

## Проверка после деплоя

1. **Основной эндпоинт**: `GET /api/recommendation/topics`
2. **Вопросы по теме**: `GET /api/recommendation/questions?topics=energy`
3. **Отправка ответов**: `POST /api/recommendation/submit`
4. **Приложение для Tilda**: `GET /` (index.html)

## Безопасность

⚠️ **Важно для production:**
1. Смените пароли в переменных окружения
2. Ограничьте CORS_ALLOWED_ORIGINS реальными доменами
3. Используйте HTTPS
4. Регулярно обновляйте зависимости

## Мониторинг

- Логи доступны через панель управления хостинга
- Проверяйте загрузку данных из Google Sheets
- Мониторьте отправку email

## Архитектура приложения

**Основные компоненты:**
- `RecommendationController` - API для рекомендаций
- `GoogleSheetsService` - загрузка данных из Google Sheets
- `RecommendationCalculationService` - логика расчета рекомендаций
- `EmailService` - отправка email с рекомендациями

**Упрощенная структура:**
- Убраны неиспользуемые контроллеры и сервисы
- Все данные загружаются из Google Sheets
- Минимальный набор API эндпоинтов
- Готово к production деплою
