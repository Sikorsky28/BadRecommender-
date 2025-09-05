# 📧 Система отправки email с рекомендациями

## Описание

Система позволяет отправлять красивые HTML-email с персональными рекомендациями БАДов пользователям на основе выбранной темы здоровья.

## Компоненты системы

### 1. HTML-шаблон email
- **Файл**: `src/main/resources/templates/email-recommendations.html`
- **Описание**: Красивый HTML-шаблон с адаптивным дизайном
- **Плейсхолдеры**:
  - `{userName}` - имя пользователя
  - `{selectedTopic}` - выбранная тема здоровья
  - `{mainRecommendations}` - HTML для основных рекомендаций (до 3 БАДов)
  - `{additionalRecommendations}` - HTML для дополнительных рекомендаций (до 2 БАДов)

### 2. EmailService
- **Файл**: `src/main/java/com/soloway/BadRecommender/service/EmailService.java`
- **Методы**:
  - `sendHtmlRecommendationsEmail()` - отправка HTML-email с рекомендациями
  - `sendEmail()` - отправка простого текстового email
  - `sendRecommendationsEmail()` - отправка текстового email с рекомендациями

### 3. RecommendationController
- **Файл**: `src/main/java/com/soloway/BadRecommender/controller/RecommendationController.java`
- **Endpoint**: `POST /api/recommendation/submit` - отправка рекомендаций с автоматической рассылкой email

### 4. RecommendationService
- **Файл**: `src/main/java/com/soloway/BadRecommender/service/RecommendationService.java`
- **Новые методы**:
  - `getMainRecommendations(String topicCode)` - получение основных рекомендаций
  - `getAdditionalRecommendations(String topicCode)` - получение дополнительных рекомендаций

### 5. Модель Supplement
- **Файл**: `src/main/java/com/soloway/BadRecommender/model/Supplement.java`
- **Новое поле**: `type` - тип БАДа ("основные" или "дополнительные")

## Использование

### 1. Через API (основной способ)

```bash
POST /api/recommendation/submit
Content-Type: application/json

{
  "email": "user@example.com",
  "userName": "Иван",
  "selectedTopic": "energy",
  "answers": [...],
  "consentPd": true,
  "consentMarketing": true
}
```

### 2. Через веб-форму

Откройте: `https://your-domain.com/` (основная форма опроса)

### 3. Программно

```java
@Autowired
private EmailService emailService;

public void sendRecommendations(String userEmail, String userName, String topicCode, 
                               List<Supplement> mainRecommendations, 
                               List<Supplement> additionalRecommendations) {
    emailService.sendHtmlRecommendationsEmail(
        userEmail, userName, topicCode, 
        mainRecommendations, additionalRecommendations
    );
}
```

## Конфигурация

### Переменные окружения

```yaml
# Email настройки
MAIL_HOST: smtp.msndr.net
MAIL_PORT: 465
MAIL_USERNAME: marketing@soloways.ru
MAIL_PASSWORD: your-password

# Google Sheets
GOOGLE_SHEETS_SPREADSHEET_ID: your-spreadsheet-id
```

### Структура Google Sheets

Лист "Supplements" должен содержать колонки:
- A: ID
- B: Название БАДа
- C: Категория (тема здоровья)
- D: Тип ("основные" или "дополнительные")
- E: Описание
- F: URL изображения
- G: URL продукта
- H: Цена

## Доступные темы

- `iron` - Поднять гемоглобин
- `energy` - Бодрость и энергия
- `sleep` - Крепкий сон, меньше стресса
- `weight` - Контроль веса и аппетита
- `skin` - Чистая кожа, крепкие волосы
- `digestion` - Комфорт пищеварения
- `joints` - Подвижные суставы, крепкие кости
- `immunity` - Сильный иммунитет
- `heart` - Здоровое сердце и сосуды
- `thyroid` - Поддержка щитовидной железы
- `female` - Регулярный цикл, мягкий ПМС
- `menopause` - Менопауза без приливов
- `male` - Мужское здоровье

## Тестирование

1. Откройте `https://your-domain.com/`
2. Заполните форму опроса с тестовыми данными
3. Укажите email и дайте согласие на обработку ПД
4. Нажмите "Получить рекомендации"
5. Проверьте email получателя

## Логирование

Система логирует:
- ✅ Успешную отправку email
- ❌ Ошибки отправки
- 📊 Загрузку данных из Google Sheets
- 🔍 Процесс фильтрации рекомендаций

## Безопасность

- Email отправляется только с настроенного SMTP сервера
- Пароль email хранится в переменных окружения
- CORS настроен для разрешенных доменов
- Валидация входных данных в контроллере
