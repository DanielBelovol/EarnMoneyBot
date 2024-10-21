package com.example.demo.data;

import com.example.demo.service.ChannelService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
@RequiredArgsConstructor
public class UserTaskState {
    private final HashMap<Long, Integer> userTaskIndex = new HashMap<>();
    private final ChannelService channelService; // Зависимость для получения каналов

    public int getCurrentTaskIndex(long chatId) {
        return userTaskIndex.getOrDefault(chatId, 0); // Возвращает 0, если задания не были получены
    }

    public void setCurrentTaskIndex(long chatId, int index) {
        userTaskIndex.put(chatId, index);
    }

    // Исправленный метод для получения всех каналов
    public List<Channel> getAllChannels() {
        return channelService.getAllChannel(); // Возвращаем список каналов
    }
}

