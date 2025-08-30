package com.soloway.BadRecommender.controller;

import com.soloway.BadRecommender.service.GoogleSheetsAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analyze")
@CrossOrigin(origins = "*")
public class GoogleSheetsAnalyzerController {

    private final GoogleSheetsAnalyzerService analyzerService;

    @Autowired
    public GoogleSheetsAnalyzerController(GoogleSheetsAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    /**
     * Анализирует структуру Google Sheets
     */
    @GetMapping("/sheets")
    public Map<String, Object> analyzeSheets() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("🔍 Запрос на анализ Google Sheets...");
            
            List<String> sheetNames = analyzerService.getAllSheetNames();
            
            result.put("status", "success");
            result.put("sheets", sheetNames);
            result.put("totalSheets", sheetNames.size());
            
            // Проверяем существование ожидаемых листов
            Map<String, Boolean> expectedSheets = new HashMap<>();
            expectedSheets.put("Topics", analyzerService.sheetExists("Topics"));
            expectedSheets.put("Questions", analyzerService.sheetExists("Questions"));
            expectedSheets.put("AnswerScores", analyzerService.sheetExists("AnswerScores"));
            expectedSheets.put("Supplements", analyzerService.sheetExists("Supplements"));
            expectedSheets.put("Categories", analyzerService.sheetExists("Categories"));
            
            result.put("expectedSheets", expectedSheets);
            
            System.out.println("✅ Анализ завершен успешно");
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка анализа: " + e.getMessage());
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Получает детальную информацию о конкретном листе
     */
    @GetMapping("/sheets/{sheetName}")
    public Map<String, Object> getSheetDetails(@PathVariable String sheetName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("🔍 Запрос детальной информации о листе: " + sheetName);
            Map<String, Object> sheetInfo = analyzerService.getSheetDetails(sheetName);
            result.putAll(sheetInfo);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Ошибка при получении информации о листе: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
