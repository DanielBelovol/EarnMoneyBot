package com.example.demo.service;

import com.example.demo.data.Channel;
import com.example.demo.repositories.ChannelRepository;
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

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final TelegramLongPollingBot telegramBot; // добавляем бота
    private final UserService userService;

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

    public boolean isSubscribed(long userId, long channelId) {
        try {
            // Создаем запрос для получения информации о пользователе в контексте канала
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(channelId); // Устанавливаем ID канала
            getChatMember.setUserId(userId);    // Устанавливаем ID пользователя

            // Отправляем запрос
            ChatMember chatMember = telegramBot.execute(getChatMember);

            // Проверяем статус пользователя
            String status = chatMember.getStatus();
            if (status.equals("member") || status.equals("administrator") || status.equals("creator")) {
                Channel channel = getById(channelId);
                channel.setSubscribed(channel.getSubscribed()+1);
            }
            return status.equals("member") || status.equals("administrator") || status.equals("creator");

        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false; // Если возникла ошибка, возвращаем false
        }
    }

    public void addChannel(String link, String name, String amdinName) {
        Channel channel = new Channel();
        channel.setLink(link);
        channel.setName(name);
        channel.setSubscribed(0);
        if (amdinName.equals("-")) {
            channel.setAdminName("No information about admin");
        } else {
            channel.setAdminName(amdinName);
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

    public
}
