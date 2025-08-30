package com.soloway.BadRecommender.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsAnalyzerService {

    private final Sheets sheetsService;
    private final String spreadsheetId;

    @Autowired
    public GoogleSheetsAnalyzerService(Sheets sheetsService,
                                      @Value("${google.sheets.spreadsheet-id}") String spreadsheetId) {
        this.sheetsService = sheetsService;
        this.spreadsheetId = spreadsheetId;
    }

    /**
     * Анализирует структуру Google Sheets и возвращает список всех листов
     */
    public List<String> getAllSheetNames() throws IOException {
        System.out.println("🔍 Анализируем структуру Google Sheets...");
        
        try {
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
            List<String> sheetNames = spreadsheet.getSheets().stream()
                .map(sheet -> sheet.getProperties().getTitle())
                .collect(Collectors.toList());
            
            System.out.println("📊 Найдены листы:");
            for (String name : sheetNames) {
                System.out.println("   - " + name);
            }
            
            return sheetNames;
        } catch (Exception e) {
            System.err.println("❌ Ошибка анализа Google Sheets: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Проверяет существование конкретного листа
     */
    public boolean sheetExists(String sheetName) throws IOException {
        List<String> allSheets = getAllSheetNames();
        return allSheets.contains(sheetName);
    }

    /**
     * Получает информацию о структуре листа (количество строк и столбцов)
     */
    public String getSheetInfo(String sheetName) throws IOException {
        try {
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
            
            return spreadsheet.getSheets().stream()
                .filter(sheet -> sheet.getProperties().getTitle().equals(sheetName))
                .findFirst()
                .map(sheet -> {
                    var props = sheet.getProperties();
                    var grid = props.getGridProperties();
                    return String.format("Лист '%s': %d строк, %d столбцов", 
                        sheetName, 
                        grid.getRowCount(), 
                        grid.getColumnCount());
                })
                .orElse("Лист '" + sheetName + "' не найден");
                
        } catch (Exception e) {
            return "Ошибка получения информации о листе '" + sheetName + "': " + e.getMessage();
        }
    }

    /**
     * Получает детальную информацию о конкретном листе
     */
    public Map<String, Object> getSheetDetails(String sheetName) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
            List<String> existingSheets = spreadsheet.getSheets().stream()
                .map(sheet -> sheet.getProperties().getTitle())
                .collect(Collectors.toList());
            
            if (!existingSheets.contains(sheetName)) {
                result.put("exists", false);
                result.put("status", "error");
                result.put("message", "Лист '" + sheetName + "' не найден");
                return result;
            }
            
            result.put("exists", true);
            result.put("status", "success");
            
            // Получаем размеры листа
            Sheet sheet = spreadsheet.getSheets().stream()
                .filter(s -> s.getProperties().getTitle().equals(sheetName))
                .findFirst()
                .orElse(null);
            
            if (sheet != null) {
                GridProperties grid = sheet.getProperties().getGridProperties();
                result.put("rows", grid.getRowCount());
                result.put("columns", grid.getColumnCount());
                result.put("info", "Лист '" + sheetName + "': " + grid.getRowCount() + " строк, " + grid.getColumnCount() + " столбцов");
            }
            
            // Получаем первые 5 строк для анализа структуры
            try {
                ValueRange range = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, sheetName + "!A1:E5")
                    .execute();
                
                if (range.getValues() != null && !range.getValues().isEmpty()) {
                    result.put("sampleData", range.getValues());
                    result.put("hasData", true);
                } else {
                    result.put("hasData", false);
                }
            } catch (Exception e) {
                result.put("hasData", false);
                result.put("dataError", e.getMessage());
            }
            
        } catch (Exception e) {
            result.put("exists", false);
            result.put("status", "error");
            result.put("message", "Ошибка при анализе листа: " + e.getMessage());
        }
        
        return result;
    }
}
