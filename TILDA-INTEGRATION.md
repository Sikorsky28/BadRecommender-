# Инструкция по интеграции опросника SOLOWAYS с Tilda

## 📋 Обзор

Опросник SOLOWAYS - это готовое веб-приложение для получения персональных рекомендаций БАДов. Приложение полностью готово для интеграции с любым сайтом на Tilda в виде всплывающего окна.

## 🚀 Быстрая интеграция

### Шаг 1: Получите URL приложения
После деплоя вашего приложения, опросник будет доступен по адресу:
```
https://your-domain.com/
```

### Шаг 2: Добавьте кнопку на Tilda
1. Откройте редактор Tilda
2. Добавьте кнопку или изображение
3. В настройках кнопки выберите "Открыть в модальном окне"
4. Вставьте URL приложения

### Шаг 3: Настройте модальное окно
1. В настройках модального окна:
   - **Ширина**: 600px
   - **Высота**: 800px
   - **Отступы**: 20px
   - **Скругление углов**: 20px

## 🔧 Детальная настройка

### Вариант 1: Через HTML-код (рекомендуется)

Добавьте этот код в HTML-код страницы Tilda:

```html
<!-- Кнопка для открытия виджета -->
<button id="open-widget-btn" style="
    background: #55685BE5;
    color: white;
    border: none;
    padding: 15px 30px;
    border-radius: 10px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.3s ease;
">
    Получить рекомендации
</button>

<!-- Модальное окно -->
<div id="widget-modal" style="
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    z-index: 9999;
    backdrop-filter: blur(5px);
">
    <div style="
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 90%;
        max-width: 600px;
        height: 90%;
        max-height: 800px;
        background: white;
        border-radius: 20px;
        overflow: hidden;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
    ">
        <button id="close-widget-btn" style="
            position: absolute;
            top: 15px;
            right: 15px;
            background: rgba(0, 0, 0, 0.1);
            border: none;
            width: 30px;
            height: 30px;
            border-radius: 50%;
            cursor: pointer;
            z-index: 10000;
            font-size: 18px;
            color: #333;
        ">×</button>
                 <iframe id="widget-iframe" 
             src="https://your-domain.com/"
            style="
                width: 100%;
                height: 100%;
                border: none;
                border-radius: 20px;
            "
        ></iframe>
    </div>
</div>

<script>
// Открытие виджета
document.getElementById('open-widget-btn').addEventListener('click', function() {
    document.getElementById('widget-modal').style.display = 'block';
    document.body.style.overflow = 'hidden';
});

// Закрытие виджета
document.getElementById('close-widget-btn').addEventListener('click', function() {
    document.getElementById('widget-modal').style.display = 'none';
    document.body.style.overflow = 'auto';
});

// Закрытие по клику вне виджета
document.getElementById('widget-modal').addEventListener('click', function(e) {
    if (e.target === this) {
        this.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
});

// Обработка сообщений от виджета
window.addEventListener('message', function(event) {
    if (event.data.type === 'WIDGET_READY') {
        console.log('Виджет SOLOWAYS готов к работе');
    }
    
    if (event.data.type === 'WIDGET_CLOSE') {
        document.getElementById('widget-modal').style.display = 'none';
        document.body.style.overflow = 'auto';
    }
});
</script>
```

### Вариант 2: Через Zero Block

1. Добавьте Zero Block на страницу
2. Вставьте HTML-код выше
3. Настройте стили под ваш дизайн

### Вариант 3: Через Popup Tilda

1. Создайте новый Popup в Tilda
2. В настройках выберите "HTML-код"
3. Вставьте только iframe:
```html
<iframe src="https://your-domain.com/" 
    style="width: 100%; height: 600px; border: none; border-radius: 20px;"></iframe>
```

## 🎨 Кастомизация

### Изменение цветов
Приложение использует CSS-переменные. Для изменения цветов отредактируйте файл `index.html`:

```css
:root {
    --primary-green: #55685BE5;    /* Основной зеленый */
    --text-dark: #2A5F4E;          /* Темный текст */
    --text-light: #FFFFFF;         /* Светлый текст */
    --background-light: #F8F6F2;   /* Светлый фон */
    --accent-gold: #E8D5B7;        /* Акцентный золотой */
}
```

### Изменение размеров
Для изменения размеров модального окна отредактируйте CSS в HTML-коде:

```css
width: 90%;        /* Ширина окна */
max-width: 600px;  /* Максимальная ширина */
height: 90%;       /* Высота окна */
max-height: 800px; /* Максимальная высота */
```

## 📱 Адаптивность

Приложение автоматически адаптируется под мобильные устройства:
- На экранах меньше 480px уменьшаются отступы
- Шрифты автоматически масштабируются
- Кнопки остаются удобными для нажатия

## 🔗 Интеграция с аналитикой

### Google Analytics
Добавьте в HTML-код Tilda:

```javascript
// Отслеживание открытия виджета
document.getElementById('open-widget-btn').addEventListener('click', function() {
    if (typeof gtag !== 'undefined') {
        gtag('event', 'widget_open', {
            'event_category': 'engagement',
            'event_label': 'soloways_widget'
        });
    }
});

// Отслеживание завершения опросника
window.addEventListener('message', function(event) {
    if (event.data.type === 'SURVEY_COMPLETED') {
        if (typeof gtag !== 'undefined') {
            gtag('event', 'survey_completed', {
                'event_category': 'conversion',
                'event_label': 'soloways_recommendations'
            });
        }
    }
});
```

### Яндекс.Метрика
```javascript
// Отслеживание открытия виджета
document.getElementById('open-widget-btn').addEventListener('click', function() {
    if (typeof ym !== 'undefined') {
        ym(12345678, 'reachGoal', 'widget_open');
    }
});
```

## 🛠️ Технические требования

### Сервер
- HTTPS обязателен для production
- CORS настроен для домена Tilda
- Поддержка iframe

### Браузеры
- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## 🔒 Безопасность

### CORS настройки
В `application.yaml` настройте разрешенные домены:

```yaml
cors:
  allowed-origins: "https://your-tilda-site.com,https://www.your-tilda-site.com"
```

### CSP (Content Security Policy)
Добавьте в HTML-код Tilda:

```html
<meta http-equiv="Content-Security-Policy" content="frame-src 'self' https://your-domain.com;">
```

## 📊 Мониторинг

### Логи
Приложение отправляет события в консоль браузера:
- `APPLICATION_READY` - приложение загружено
- `SURVEY_STARTED` - начат опросник
- `SURVEY_COMPLETED` - опросник завершен
- `EMAIL_SENT` - email отправлен

### Ошибки
При ошибках приложение показывает пользователю понятные сообщения и логирует детали в консоль.

## 🚀 Готовые решения

### Кнопка "Получить рекомендации"
```html
<div style="text-align: center; padding: 40px 0;">
    <button id="open-widget-btn" style="
        background: linear-gradient(135deg, #55685BE5 0%, #4A5A4E 100%);
        color: white;
        border: none;
        padding: 20px 40px;
        border-radius: 15px;
        font-family: 'Montserrat', sans-serif;
        font-size: 18px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 10px 30px rgba(85, 104, 91, 0.3);
    ">
        🎯 Получить персональные рекомендации
    </button>
</div>
```

### Баннер с призывом к действию
```html
<div style="
    background: linear-gradient(135deg, #F8F6F2 0%, #FFFFFF 100%);
    border: 2px solid #E8D5B7;
    border-radius: 20px;
    padding: 30px;
    text-align: center;
    margin: 20px 0;
">
    <h3 style="color: #2A5F4E; font-family: 'Montserrat', sans-serif; margin-bottom: 15px;">
        Не знаете, какие БАДы подходят именно вам?
    </h3>
    <p style="color: #666; margin-bottom: 20px;">
        Пройдите короткий опрос и получите персональные рекомендации от экспертов SOLOWAYS
    </p>
    <button id="open-widget-btn" style="
        background: #55685BE5;
        color: white;
        border: none;
        padding: 15px 30px;
        border-radius: 10px;
        font-family: 'Montserrat', sans-serif;
        font-weight: 600;
        cursor: pointer;
    ">
        Начать опрос
    </button>
</div>
```

## 📞 Поддержка

При возникновении проблем:
1. Проверьте консоль браузера на ошибки
2. Убедитесь, что домен виджета доступен
3. Проверьте настройки CORS на сервере
4. Обратитесь к технической поддержке

---

**Опросник SOLOWAYS готов к интеграции!** 🎉
