package com.example.demo.service;

import com.example.demo.data.Channel;
import com.example.demo.data.ChannelSubscriber;
import com.example.demo.data.User;
import com.example.demo.repositories.ChannelRepository;
import com.example.demo.repositories.ChannelSubscribersRepository;
import com.example.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.List;

import static ch.qos.logback.core.joran.spi.ConsoleTarget.findByName;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final TelegramLongPollingBot telegramBot; // добавляем бота
    private final UserRepository userRepository;
    private final ChannelSubscribersRepository channelSubscribersRepository;

    public List<Channel> getAllChannel() {
        return channelRepository.findAll();
    }

    public Long getChannelIdByUsername(String channelUsername) {
        try {
            // Создаем запрос для получения информации о канале
            GetChat getChat = new GetChat();
            getChat.setChatId(channelUsername);  // Устанавливаем username канала (например, "@channel_username")

            // Выполняем запрос через экземпляр бота
            Chat chat = telegramBot.execute(getChat);  // используем telegramBot для выполнения запроса

            // Возвращаем ID канала
            return chat.getId();

        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null; // В случае ошибки возвращаем null
        }
    }

    public List<User> getChannelSubscribers(long channelId) {
        return userRepository.findUsersByChannelId(channelId);
    }

    public boolean subscribeUserToChannel(long userId, long channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new RuntimeException("Channel not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, не подписан ли уже пользователь
        if (!channelSubscribersRepository.existsByChannelIdAndUserId(channelId, userId)) {
            ChannelSubscriber subscriber = new ChannelSubscriber();
            subscriber.setChannelId(channelId);
            subscriber.setUserId(userId);
            channelSubscribersRepository.save(subscriber);
            return true; // Подписка успешна
        }
        return false; // Уже подписан
    }

    public void addChannel(String link, String name, String adminName) {
        Channel channel = new Channel();
        channel.setLink(link);
        channel.setName(name);
        if (adminName.equals("-")) {
            channel.setAdminName("No information about admin");
        } else {
            channel.setAdminName(adminName);
        }
        channelRepository.save(channel);
    }


    public void removeChannel(String link) {
        channelRepository.deleteByLink(link);
    }

    public void updateChannel(String link, String name, String adminName) {
        Channel channel = channelRepository.findByLink(link);
        channel.setName(name);
        channel.setAdminName(adminName);

        channelRepository.save(channel);
    }

    public Channel getById(long id) {
        return channelRepository.getReferenceById(id);
    }

    public String getChannelStatistics(String channelName) {
        Channel channel = channelRepository.findByName(channelName);
        long channelId = getChannelIdByUsername(channelName);
        if (channel != null) {
            List<User> subscribers = getChannelSubscribers(channelId); // Получаем всех подписчиков
            int subscribedCount = subscribers.size();

            StringBuilder statistics = new StringBuilder();
            statistics.append("Подписчиков: ").append(subscribedCount).append("\n");
            statistics.append("Список подписчиков: ").append("\n");

            for (User user : subscribers) {
                statistics.append("- ").append(user.getUserName()).append("\n");
            }

            return statistics.toString();
        } else {
            throw new RuntimeException("Channel not found for ID: " + channelId);
        }
    }
}
