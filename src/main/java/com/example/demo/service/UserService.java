package com.example.demo.service;

import com.example.demo.data.User;
import com.example.demo.data.UserRole;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public boolean isExist(long userId){
        return userRepository.existsById(userId);
    }
    public void create(long userId, String username){
        userRepository.save(new User(userId,username,0, UserRole.USER_ROLE));
    }
    public User getUserByUserId(long userId){
        return userRepository.findById(userId).get();
    }
    @Transactional
    public void earn(long userId) {
        // Получаем пользователя из базы данных
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Обновляем баланс пользователя
            user.setMoney(user.getMoney() + 5);
            userRepository.save(user); // Сохраняем изменения
        } else {
            // Логика обработки ситуации, если пользователь не найден
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
    public boolean isAdmin(long userId){
        Optional<User> user = userRepository.findById(userId);
        if(user.get().getUserRole()==UserRole.USER_ROLE){
            return true;
        }else {
            return false;
        }
    }

}
