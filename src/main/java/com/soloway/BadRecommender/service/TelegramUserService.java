package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.TelegramUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для управления пользователями Telegram
 */
@Service
public class TelegramUserService {

    private final Map<Long, TelegramUser> users = new ConcurrentHashMap<>();

    /**
     * Получить или создать пользователя
     */
    public TelegramUser getUser(Long chatId) {
        return users.computeIfAbsent(chatId, TelegramUser::new);
    }

    /**
     * Обновить информацию о пользователе
     */
    public void updateUser(TelegramUser user) {
        users.put(user.getChatId(), user);
    }

    /**
     * Удалить пользователя
     */
    public void removeUser(Long chatId) {
        users.remove(chatId);
    }

    /**
     * Очистить неактивных пользователей
     */
    public void cleanupInactiveUsers() {
        users.entrySet().removeIf(entry -> !entry.getValue().isActive());
    }

    /**
     * Получить количество активных пользователей
     */
    public int getActiveUsersCount() {
        return (int) users.values().stream()
                .filter(TelegramUser::isActive)
                .count();
    }

    /**
     * Получить всех пользователей
     */
    public Map<Long, TelegramUser> getAllUsers() {
        return new ConcurrentHashMap<>(users);
    }
}
