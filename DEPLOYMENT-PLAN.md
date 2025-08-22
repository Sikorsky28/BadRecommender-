# 🚀 Финальный план деплоя SOLOWAYS BadRecommender

## ✅ Состояние проекта после очистки

### **Удаленные неиспользуемые файлы:**
- ❌ `AdminController.java` - админка не нужна
- ❌ `DataInitializationController.java` - данные уже в Google Sheets  
- ❌ `SupplementController.java` - CRUD операции не нужны
- ❌ `SupplementService.java` - заменен на GoogleSheetsService
- ❌ `TestEmailService.java` - тестовый сервис
- ❌ `tilda-widget.html` - используем основной index.html
- ❌ `test-widget.html` - тестовая страница не нужна

### **Оставшиеся компоненты:**
- ✅ `RecommendationController` - основной API
- ✅ `GoogleSheetsService` - загрузка данных из Google Sheets
- ✅ `RecommendationCalculationService` - логика рекомендаций
- ✅ `EmailService` - отправка email
- ✅ `index.html` - готовое приложение для Tilda

---

## 🌐 Пошаговый план деплоя

### **Этап 1: Подготовка (5 минут)**

#### **1.1 Проверка готовности**
```bash
# Убедиться что все работает локально
./gradlew clean build -x test

# Проверить что JAR создался
ls build/libs/
```

#### **1.2 Подготовка файлов**
- ✅ `BadRecommender-0.0.1-SNAPSHOT.jar` - готовый JAR
- ✅ `google-credentials.json` - Google Sheets API ключи
- ✅ `Dockerfile` - для контейнеризации
- ✅ `render.yaml` - конфигурация Render.com

---

### **Этап 2: GitHub репозиторий (10 минут)**

#### **2.1 Создание репозитория**
```bash
# Инициализация Git
git init
git add .
git commit -m "SOLOWAYS BadRecommender - готов к деплою"

# Создать репозиторий на GitHub и добавить remote
git remote add origin https://github.com/your-username/BadRecommender.git
git branch -M main
git push -u origin main
```

---

### **Этап 3: Деплой на Render.com (15 минут)**

#### **3.1 Создание сервиса**
1. **New → Web Service**
2. **Connect Repository** → выбрать GitHub репозиторий
3. **Configure Service:**
   - **Name:** `soloways-bad-recommender`
   - **Environment:** `Java`
   - **Build Command:** `./gradlew clean build -x test`
   - **Start Command:** `java -jar build/libs/BadRecommender-0.0.1-SNAPSHOT.jar`
   - **Plan:** `Free`

#### **3.2 Настройка переменных окружения**
```
GOOGLE_SHEETS_SPREADSHEET_ID=your_google_sheets_id
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password
GOOGLE_CREDENTIALS_FILE=google-credentials.json
CORS_ALLOWED_ORIGINS=https://your-tilda-site.com
```

#### **3.3 Загрузка Google credentials**
1. **Files** → загрузить `google-credentials.json`
2. **Manual Deploy** → перезапустить сервис

---

### **Этап 4: Тестирование (10 минут)**

#### **4.1 Проверка API**
```bash
# Основной эндпоинт
curl https://your-app-name.onrender.com/api/recommendation/topics

# Главная страница
curl https://your-app-name.onrender.com/

# Тест описаний
curl https://your-app-name.onrender.com/api/recommendation/test-descriptions
```

#### **4.2 Проверка функционала**
1. **Открыть приложение** в браузере
2. **Пройти опросник** полностью
3. **Проверить отправку email**
4. **Убедиться в корректности рекомендаций**

---

### **Этап 5: Интеграция с Tilda (5 минут)**

#### **5.1 Получить URL приложения**
```
https://your-app-name.onrender.com/
```

#### **5.2 Добавить в Tilda**
```html
<!-- Кнопка для открытия -->
<button onclick="openSoloways()">
    Получить рекомендации
</button>

<!-- Модальное окно -->
<div id="soloways-modal" style="display: none;">
    <iframe src="https://your-app-name.onrender.com/" 
            style="width: 100%; height: 600px; border: none;">
    </iframe>
</div>

<script>
function openSoloways() {
    document.getElementById('soloways-modal').style.display = 'block';
}
</script>
```

---

## 🔧 Альтернативные варианты деплоя

### **Вариант B: Docker**
```bash
# Сборка образа
docker build -t soloways-bad-recommender .

# Запуск контейнера
docker run -d \
  --name soloways-app \
  -p 8080:8080 \
  -e GOOGLE_SHEETS_SPREADSHEET_ID=your_id \
  -e MAIL_USERNAME=your_email \
  -e MAIL_PASSWORD=your_password \
  soloways-bad-recommender
```

### **Вариант C: Heroku**
```bash
# Создать Procfile
echo "web: java -jar build/libs/BadRecommender-0.0.1-SNAPSHOT.jar" > Procfile

# Деплой
heroku create soloways-bad-recommender
heroku config:set GOOGLE_SHEETS_SPREADSHEET_ID=your_id
heroku config:set MAIL_USERNAME=your_email
heroku config:set MAIL_PASSWORD=your_password
git push heroku main
```

---

## 📊 Чек-лист готовности

- [ ] **Локальное тестирование** пройдено
- [ ] **JAR файл** собран и протестирован
- [ ] **Google credentials** готовы
- [ ] **GitHub репозиторий** создан
- [ ] **Переменные окружения** подготовлены
- [ ] **CORS настройки** проверены
- [ ] **Email сервис** настроен
- [ ] **Неиспользуемые файлы** удалены
- [ ] **Документация** обновлена

---

## 🎯 Результат

**После выполнения плана:**
- ✅ Приложение работает в production
- ✅ Доступно по URL: `https://your-app-name.onrender.com/`
- ✅ Готово к интеграции с Tilda
- ✅ Все API эндпоинты работают
- ✅ Email отправка настроена
- ✅ Данные загружаются из Google Sheets

---

## 🚨 Возможные проблемы

### **Проблема: Приложение не запускается**
```bash
# Проверить логи в Render Dashboard
# Проверить переменные окружения
# Убедиться что google-credentials.json загружен
```

### **Проблема: CORS ошибки**
```yaml
# Проверить application.yaml
cors:
  allowed-origins: https://your-tilda-site.com
```

### **Проблема: Google Sheets API недоступен**
```bash
# Проверить google-credentials.json
# Проверить права доступа к Google Sheets
# Проверить ID таблицы
```

---

## 📞 Поддержка

- **Render.com Support** - для технических вопросов
- **GitHub Issues** - для багов и улучшений
- **SOLOWAYS Team** - для бизнес-логики

---

**🎉 Готово! Ваше приложение SOLOWAYS BadRecommender готово к деплою!**

**Общее время выполнения: 45 минут**
**Сложность: Средняя**
**Риски: Минимальные**
