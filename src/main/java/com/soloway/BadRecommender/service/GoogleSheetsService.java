package com.soloway.BadRecommender.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.soloway.BadRecommender.model.Supplement;
import com.soloway.BadRecommender.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsService {

    private final Sheets sheetsService;
    private final String spreadsheetId;

    // Названия листов в Google Sheets
    private static final String SUPPLEMENTS_SHEET = "Supplements";
    private static final String CATEGORIES_SHEET = "Categories";

    @Autowired
    public GoogleSheetsService(Sheets sheetsService, @Value("${google.sheets.spreadsheet-id}") String spreadsheetId) {
        this.sheetsService = sheetsService;
        this.spreadsheetId = spreadsheetId;
    }

    /**
     * Загружает все добавки из Google Sheets
     */
    public List<Supplement> loadSupplements() throws IOException {
        System.out.println("📊 Загружаем добавки из Google Sheets...");
        
        String range = SUPPLEMENTS_SHEET + "!A2:J"; // Пропускаем заголовок, включаем колонки H-J для описания, изображения и цены
        System.out.println("🔍 Запрашиваем диапазон: " + range);
        
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        System.out.println("📋 Получено строк из Google Sheets: " + (values != null ? values.size() : 0));
        
        if (values == null || values.isEmpty()) {
            System.out.println("⚠️ Данные не найдены в Google Sheets");
            return new ArrayList<>();
        }

        List<Supplement> supplements = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            System.out.println("🔍 Обрабатываем строку " + (i + 1) + ": " + row);
            try {
                Supplement supplement = parseSupplementFromRow(row);
                if (supplement != null) {
                    supplements.add(supplement);
                    System.out.println("✅ Добавлена добавка: " + supplement.getName());
                } else {
                    System.out.println("❌ Строка не распарсена: " + row);
                }
            } catch (Exception e) {
                System.err.println("❌ Ошибка парсинга строки: " + row + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("✅ Загружено добавок: " + supplements.size());
        return supplements;
    }

    /**
     * Загружает категории из Google Sheets
     */
    public List<Category> loadCategories() throws IOException {
        System.out.println("📊 Загружаем категории из Google Sheets...");
        
        String range = CATEGORIES_SHEET + "!A2:B"; // Пропускаем заголовок
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("⚠️ Категории не найдены в Google Sheets");
            return new ArrayList<>();
        }

        List<Category> categories = new ArrayList<>();
        for (List<Object> row : values) {
            try {
                if (row.size() >= 2) {
                    Long id = Long.parseLong(row.get(0).toString());
                    String name = row.get(1).toString();
                    categories.add(new Category(name));
                }
            } catch (Exception e) {
                System.err.println("❌ Ошибка парсинга категории: " + row + " - " + e.getMessage());
            }
        }

        System.out.println("✅ Загружено категорий: " + categories.size());
        return categories;
    }

    /**
     * Сохраняет добавку в Google Sheets
     */
    public void saveSupplement(Supplement supplement) throws IOException {
        List<Object> row = Arrays.asList(
                supplement.getId(),
                supplement.getCode(),
                supplement.getName(),
                supplement.getCategory().getName(),
                supplement.isActive() ? "TRUE" : "FALSE",
                supplement.getTags().stream().collect(Collectors.joining(",")),
                supplement.getProductUrl() != null ? supplement.getProductUrl() : "", // URL товара
                supplement.getDescription() != null ? supplement.getDescription() : "", // Описание
                supplement.getImageUrl() != null ? supplement.getImageUrl() : "", // URL изображения
                supplement.getPrice() != null ? supplement.getPrice() : "" // Цена
        );

        // Находим следующую пустую строку
        String range = SUPPLEMENTS_SHEET + "!A:A";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        int nextRow = (response.getValues() != null ? response.getValues().size() : 1) + 1;
        String writeRange = SUPPLEMENTS_SHEET + "!A" + nextRow + ":J" + nextRow;

        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(row));

        sheetsService.spreadsheets().values()
                .update(spreadsheetId, writeRange, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("✅ Добавка сохранена в Google Sheets: " + supplement.getName());
    }

    /**
     * Обновляет добавку в Google Sheets
     */
    public void updateSupplement(Supplement supplement) throws IOException {
        List<Object> row = Arrays.asList(
                supplement.getId(),
                supplement.getCode(),
                supplement.getName(),
                supplement.getCategory().getName(),
                supplement.isActive() ? "TRUE" : "FALSE",
                supplement.getTags().stream().collect(Collectors.joining(",")),
                supplement.getProductUrl() != null ? supplement.getProductUrl() : "", // URL товара
                supplement.getDescription() != null ? supplement.getDescription() : "", // Описание
                supplement.getImageUrl() != null ? supplement.getImageUrl() : "", // URL изображения
                supplement.getPrice() != null ? supplement.getPrice() : "" // Цена
        );

        // Находим строку с нужным ID
        String range = SUPPLEMENTS_SHEET + "!A:A";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        int rowIndex = -1;
        
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).size() > 0 && supplement.getId().toString().equals(values.get(i).get(0).toString())) {
                rowIndex = i + 1; // +1 потому что индексы в Google Sheets начинаются с 1
                break;
            }
        }

        if (rowIndex == -1) {
            throw new IOException("Добавка с ID " + supplement.getId() + " не найдена");
        }

        String writeRange = SUPPLEMENTS_SHEET + "!A" + rowIndex + ":I" + rowIndex;
        ValueRange body = new ValueRange().setValues(Arrays.asList(row));
        
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, writeRange, body)
                .setValueInputOption("RAW")
                .execute();
        
        System.out.println("✅ Добавка обновлена: " + supplement.getName() + " (ID: " + supplement.getId() + ")");
    }

    /**
     * Деактивирует добавку в Google Sheets
     */
    public void deactivateSupplement(Long id) throws IOException {
        // Находим строку с нужным ID
        String range = SUPPLEMENTS_SHEET + "!A:A";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        int rowIndex = -1;
        
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).size() > 0 && id.toString().equals(values.get(i).get(0).toString())) {
                rowIndex = i + 1; // +1 потому что индексы в Google Sheets начинаются с 1
                break;
            }
        }

        if (rowIndex == -1) {
            throw new IOException("Добавка с ID " + id + " не найдена");
        }

        // Обновляем только колонку Active
        String writeRange = SUPPLEMENTS_SHEET + "!E" + rowIndex;
        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList("FALSE")));
        
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, writeRange, body)
                .setValueInputOption("RAW")
                .execute();
        
        System.out.println("✅ Добавка деактивирована: ID " + id);
    }

    /**
     * Удаляет добавку из Google Sheets
     */
    public void deleteSupplement(Long id) throws IOException {
        // Находим строку с этой добавкой
        String range = SUPPLEMENTS_SHEET + "!A2:G";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null) {
            throw new IOException("Данные не найдены");
        }

        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && id.toString().equals(row.get(0).toString())) {
                // Найдена строка, удаляем её (заменяем на пустые значения)
                String deleteRange = SUPPLEMENTS_SHEET + "!A" + (i + 2) + ":G" + (i + 2);
                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(Arrays.asList("", "", "", "", "", "", "")));

                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, deleteRange, body)
                        .setValueInputOption("RAW")
                        .execute();

                System.out.println("✅ Добавка удалена из Google Sheets: ID " + id);
                return;
            }
        }

        throw new IOException("Добавка с ID " + id + " не найдена");
    }

    /**
     * Парсит строку из Google Sheets в объект Supplement
     */
    private Supplement parseSupplementFromRow(List<Object> row) {
        if (row.size() < 6) {
            System.err.println("❌ Недостаточно колонок в строке: " + row.size() + " из " + row);
            return null;
        }

        try {
            Long id = Long.parseLong(row.get(0).toString());
            String code = row.get(1).toString();
            String name = row.get(2).toString();
            String categoryName = row.get(3).toString();
            boolean active = "TRUE".equalsIgnoreCase(row.get(4).toString());
            String tagsString = row.get(5).toString();
            
            // URL товара может отсутствовать (7-я колонка)
            String productUrl = row.size() > 6 ? row.get(6).toString() : "";
            
            // Описание (8-я колонка)
            String description = row.size() > 7 ? row.get(7).toString() : "";
            
            // URL изображения (9-я колонка)
            String imageUrl = row.size() > 8 ? row.get(8).toString() : "";
            
            // Цена (10-я колонка J)
            String price = row.size() > 9 ? row.get(9).toString() : "";

            // Парсим теги
            Set<String> tags = new HashSet<>();
            if (!tagsString.isEmpty()) {
                tags = Arrays.stream(tagsString.split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("\\s+", "")) // Убираем все пробелы и переносы строк
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
            }

            Category category = new Category(categoryName);
            Supplement supplement = new Supplement(id, code, name, category, tags, active);
            
            // Устанавливаем описание, URL товара, изображения и цену
            supplement.setDescription(description);
            supplement.setProductUrl(productUrl);
            supplement.setImageUrl(imageUrl);
            supplement.setPrice(price);

            System.out.println("✅ Успешно распарсена добавка: " + name + " (ID: " + id + ") с описанием: " + description + ", URL: " + productUrl + ", Image: " + imageUrl);
            return supplement;
        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга строки: " + row + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Записывает заголовки в указанный лист
     */
    public void writeHeaders(String sheetName, List<Object> headers) throws IOException {
        String range;
        if ("Supplements".equals(sheetName)) {
            range = sheetName + "!A1:G1";
        } else if ("Categories".equals(sheetName)) {
            range = sheetName + "!A1:B1";
        } else {
            range = sheetName + "!A1:" + getColumnLetter(headers.size()) + "1";
        }
        
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(headers));

        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("✅ Заголовки записаны в лист " + sheetName);
    }

    /**
     * Получает букву колонки по номеру
     */
    private String getColumnLetter(int columnNumber) {
        if (columnNumber <= 0) return "A";
        StringBuilder result = new StringBuilder();
        while (columnNumber > 0) {
            columnNumber--;
            result.insert(0, (char) ('A' + columnNumber % 26));
            columnNumber /= 26;
        }
        return result.toString();
    }



    /**
     * Проверяет подключение к Google Sheets
     */
    public boolean testConnection() {
        try {
            String range = SUPPLEMENTS_SHEET + "!A1:A1";
            sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            System.out.println("✅ Подключение к Google Sheets успешно");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Ошибка подключения к Google Sheets: " + e.getMessage());
            return false;
        }
    }
}

