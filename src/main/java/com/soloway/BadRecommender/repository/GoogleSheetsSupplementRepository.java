package com.soloway.BadRecommender.repository;

import com.soloway.BadRecommender.model.Supplement;
import com.soloway.BadRecommender.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GoogleSheetsSupplementRepository implements SupplementRepositoryInterface {

    private final GoogleSheetsService googleSheetsService;
    private List<Supplement> supplementsCache = new ArrayList<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5 минут

    @Autowired
    public GoogleSheetsSupplementRepository(GoogleSheetsService googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    /**
     * Обновляет кэш данных из Google Sheets
     */
    private void refreshCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_TTL) {
            try {
                System.out.println("🔄 Обновляем кэш из Google Sheets...");
                supplementsCache = googleSheetsService.loadSupplements();
                lastCacheUpdate = currentTime;
                System.out.println("✅ Кэш обновлен: " + supplementsCache.size() + " добавок");
            } catch (IOException e) {
                System.err.println("❌ Ошибка обновления кэша: " + e.getMessage());
                // Если не удалось обновить кэш, используем старые данные
                if (supplementsCache.isEmpty()) {
                    System.err.println("⚠️ Используем fallback данные");
                    supplementsCache = getFallbackSupplements();
                }
            }
        }
    }

    @Override
    public List<Supplement> getAll() {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .filter(Supplement::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Supplement> getAllIncludingInactive() {
        refreshCacheIfNeeded();
        return new ArrayList<>(supplementsCache);
    }

    @Override
    public Optional<Supplement> getById(Long id) {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Supplement> getByCategory(String category) {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .filter(s -> s.getCategory().getName().equalsIgnoreCase(category))
                .filter(Supplement::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Supplement> getByTag(String tag) {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .filter(s -> s.getTags().contains(tag.toLowerCase()))
                .filter(Supplement::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Supplement> getByTags(Set<String> tags) {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .filter(s -> s.getTags().stream().anyMatch(tags::contains))
                .filter(Supplement::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Supplement> searchByName(String name) {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(Supplement::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public Supplement save(Supplement supplement) {
        try {
            if (supplement.getId() == null) {
                // Новый элемент - генерируем ID
                Long newId = supplementsCache.stream()
                        .mapToLong(Supplement::getId)
                        .max()
                        .orElse(0) + 1;
                supplement.setId(newId);
                
                // Сохраняем в Google Sheets
                googleSheetsService.saveSupplement(supplement);
            } else {
                // Обновление существующего
                googleSheetsService.updateSupplement(supplement);
            }
            
            // Обновляем кэш
            lastCacheUpdate = 0; // Принудительно обновляем кэш
            refreshCacheIfNeeded();
            
            return supplement;
        } catch (IOException e) {
            System.err.println("❌ Ошибка сохранения в Google Sheets: " + e.getMessage());
            throw new RuntimeException("Не удалось сохранить добавку", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            googleSheetsService.deleteSupplement(id);
            
            // Обновляем кэш
            lastCacheUpdate = 0;
            refreshCacheIfNeeded();
        } catch (IOException e) {
            System.err.println("❌ Ошибка удаления из Google Sheets: " + e.getMessage());
            throw new RuntimeException("Не удалось удалить добавку", e);
        }
    }

    @Override
    public void deactivateById(Long id) {
        Optional<Supplement> supplementOpt = getById(id);
        if (supplementOpt.isPresent()) {
            Supplement supplement = supplementOpt.get();
            supplement.setActive(false);
            save(supplement);
        }
    }

    @Override
    public void activateById(Long id) {
        Optional<Supplement> supplementOpt = getById(id);
        if (supplementOpt.isPresent()) {
            Supplement supplement = supplementOpt.get();
            supplement.setActive(true);
            save(supplement);
        }
    }

    @Override
    public List<String> getAllCategories() {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .map(s -> s.getCategory().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getAllTags() {
        refreshCacheIfNeeded();
        return supplementsCache.stream()
                .flatMap(s -> s.getTags().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Fallback данные на случай недоступности Google Sheets
     */
    private List<Supplement> getFallbackSupplements() {
        List<Supplement> fallback = new ArrayList<>();
        
        // Базовые добавки для fallback
        fallback.add(new Supplement(1L, "Energy", "Базовая добавка для энергии", 
                new com.soloway.BadRecommender.model.Category("Энергия"), 
                Set.of("energy", "vitality"), true));
        
        fallback.add(new Supplement(2L, "Magnesium B6", "Поддержка нервной системы", 
                new com.soloway.BadRecommender.model.Category("Сон"), 
                Set.of("sleep", "stress", "magnesium"), true));
        
        fallback.add(new Supplement(3L, "Vitamin D3", "Поддержка иммунитета", 
                new com.soloway.BadRecommender.model.Category("Иммунитет"), 
                Set.of("immunity", "vitamin-d"), true));
        
        return fallback;
    }

    /**
     * Принудительно обновляет кэш
     */
    public void forceRefreshCache() {
        lastCacheUpdate = 0;
        refreshCacheIfNeeded();
    }

    /**
     * Проверяет подключение к Google Sheets
     */
    public boolean testConnection() {
        return googleSheetsService.testConnection();
    }
}
