package com.example.demo.service;

import com.example.demo.data.Channel;
import com.example.demo.data.User;
import com.example.demo.data.UserRole;
import com.example.demo.repositories.ChannelRepository;
import com.example.demo.repositories.ChannelSubscribersRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ChannelSubscribersRepository channelSubscribersRepository;

    public boolean isExist(long userId) {
        return userRepository.existsById(userId);
    }

    public void create(long userId, String username) {
        userRepository.save(new User(userId, username, 0, UserRole.USER_ROLE));
    }

    public User getUserByUserId(long userId) {
        return userRepository.findById(userId).get();
    }

    @Transactional
    public void earn(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Обновляем баланс пользователя
            user.setMoney(user.getMoney() + 5);
            userRepository.save(user);
        } else {
            System.out.println("Пользователь с ID " + userId + " не найден.");
        }
    }

    public String balanceText(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            long money = optionalUser.get().getMoney();
            return String.format("💰 Ваш текущий баланс: %d руб.\n\nДля вывода средств вам необходимо накопить минимум 100 руб.", money);
        } else {
            return "❗ Пользователь не найден. Пожалуйста, попробуйте снова.";
        }
    }

    public boolean isAdmin(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.get().getUserRole() == UserRole.USER_ROLE) {
            return false;
        } else {
            return true;
        }
    }

    public String viewAllUsers() {
        List<User> userList = userRepository.findAll();

        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (User user : userList) {
            sb.append(counter + " ");
            sb.append("Name: " + user.getUserName() + ", ");
            sb.append("role:" + user.getUserRole().toString() + ", ");
            sb.append("money:" + user.getMoney() + "\n");
        }
        return sb.toString();
    }

    public List<Channel> getUserSubscribedChannels(long userId) {
        return channelRepository.findChannelsByUserId(userId);
    }

    public int getUserSubscriptionCount(long userId) {
        return channelSubscribersRepository.countByUserId(userId);
    }

    public String userStats(long userId) {
        List<Channel> subscribedChannels = getUserSubscribedChannels(userId);
        int subscriptionCount = getUserSubscriptionCount(userId);

        if (subscriptionCount == 0) {
            return "Пользователь не подписан ни на один канал.";
        }

        StringBuilder statsMessage = new StringBuilder();
        statsMessage.append("Подписан на ").append(subscriptionCount).append(" канал(ов):\n");

        for (Channel channel : subscribedChannels) {
            statsMessage.append("- ").append(channel.getName()).append("\n");
        }

        return statsMessage.toString();
    }

}
