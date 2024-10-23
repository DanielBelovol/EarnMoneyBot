package com.example.demo.service;

import com.example.demo.data.Channel;
import com.example.demo.data.ChannelSubscriber;
import com.example.demo.data.User;
import com.example.demo.repositories.ChannelRepository;
import com.example.demo.repositories.ChannelSubscribersRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.ws.rs.ext.ParamConverter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ChannelSubscribersRepository channelSubscribersRepository;

    public List<Channel> getAllChannel() {
        return channelRepository.findAll();
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


}
