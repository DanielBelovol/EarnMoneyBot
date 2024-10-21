package com.example.demo.service;

import com.example.demo.config.BotConfig;
import com.example.demo.data.AdminState;
import com.example.demo.data.Channel;
import com.example.demo.data.UserRole;
import com.example.demo.data.UserTaskState;

import com.example.demo.repositories.ChannelRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TelegramBot extends TelegramLongPollingBot {
    private static final String WELCOME_TEXT = "EarningMoneyOnlineBOT помогает зарабатывать деньги всем, даже школьникам! \uD83D\uDCB8 Все, что вам нужно делать, — это выполнять простые задания, такие как подписка на каналы.\n" +
            "\n" +
            "Чтобы узнать, как именно работает бот, просто напишите команду /help или нажмите на кнопку ниже. Удачного заработка! \uD83D\uDE80\n" +
            "\n";
    private static final String HELP_TEXT = "Чтобы начать зарабатывать деньги, нажмите на кнопку \"View Tasks\". После этого откроется задание с двумя кнопками: \"Проверить\" и \"Дальше\" или \"Назад\".\n" +
            "\n" +
            "Если задание вам не подходит, вы можете пролистать его, нажав \"Дальше\", или вернуться к предыдущему заданию, нажав \"Назад\".\n" +
            "После того как вы выберете задание, выполните его и нажмите \"Проверить\". Только тогда на ваш баланс будут зачислены деньги.\n" +
            "Минимальный вывод средств составляет 100 рублей.\n" +
            "\n" +
            "Обратите внимание, что для проверки заданий необходимо подождать неделю, чтобы избежать ситуации, когда пользователь отписывается или отменяет задание.\n" +
            "\n";
    private final
    BotConfig botConfig;
    private final UserService userService;
    private final ChannelService channelService;
    private final UserTaskState userTaskState;
    private final AdminService adminService;
    private final ChannelRepository channelRepository;

    public TelegramBot(BotConfig botConfig, UserService userService, ChannelService channelService, AdminService adminService, ChannelRepository channelRepository) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.channelService = channelService;
        this.userTaskState = new UserTaskState(channelService);
        this.adminService = adminService;
        this.channelRepository = channelRepository;
        setBotCommands();
    }
    HashMap<Long, AdminState> adminStateHashMap = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();

            long chatId = message.getChatId();
            long userId = message.getFrom().getId();
            String username = message.getFrom().getUserName();

            if (!userService.isExist(userId)) {
                userService.create(userId, username);
                sendBotKeyboardWithText(chatId, WELCOME_TEXT);
            }

            switch (messageText) {
                case "/admin_panel":
                    if (userService.getUserByUserId(userId).getUserRole().equals(UserRole.ADMIN_ROLE)) {
                        adminService.sendAminPanel(chatId, "Это ваша панель.");
                        adminStateHashMap.put(userId,AdminState.NONE);
                    } else {
                        sendBotKeyboardWithText(chatId, "У вас нет доступа к панели администратора.");
                    }
                    break;
                case "/all_channels":
                    if (userService.getUserByUserId(userId).getUserRole().equals(UserRole.ADMIN_ROLE)) {
                        channelService.getAllChannel();
                    }else {
                        sendBotKeyboardWithText(chatId,"У вас нет доступа");
                    }
                    break;
                case "/add_channel":
                    String channelLink = null;
                    String channelName = null;
                    String adminName = null;
                    // Проверяем, является ли пользователь администратором
                    if (userService.getUserByUserId(userId).getUserRole().equals(UserRole.ADMIN_ROLE)) {
                        // Если текущее состояние - LINK_WRITE
                        if (adminStateHashMap.get(userId) == AdminState.LINK_WRITE) {
                            // Сохраняем ссылку на канал
                            channelLink = messageText;
                            // Здесь вы можете добавить проверку на корректность ссылки

                            // Переходим к следующему состоянию
                            adminStateHashMap.put(userId, AdminState.NAME_WRITE);
                            sendBotKeyboardWithText(chatId, "Пожалуйста, введите имя канала:");
                        }
                        // Если текущее состояние - NAME_WRITE
                        else if (adminStateHashMap.get(userId) == AdminState.NAME_WRITE) {
                            // Сохраняем имя канала
                             channelName = messageText;

                            // Переходим к следующему состоянию
                            adminStateHashMap.put(userId, AdminState.ADMIN_WRITE);
                            sendBotKeyboardWithText(chatId, "Пожалуйста, введите имя администратора канала:");
                        }
                        // Если текущее состояние - ADMIN_WRITE
                        else if (adminStateHashMap.get(userId) == AdminState.ADMIN_WRITE) {
                            // Сохраняем имя администратора
                             adminName = messageText;

                            // Создаем канал с собранными данными
                            channelService.addChannel(channelLink, channelName, adminName);
                            sendBotKeyboardWithText(chatId, "Канал успешно добавлен!");

                            // Сбрасываем состояние
                            adminStateHashMap.put(userId, AdminState.NONE);
                        }
                        // Если состояние NONE, запрашиваем ссылку
                        else if (adminStateHashMap.get(userId) == AdminState.NONE) {
                            adminStateHashMap.put(userId, AdminState.LINK_WRITE);
                            sendBotKeyboardWithText(chatId, "Пожалуйста, введите ссылку на канал:");
                        }
                    } else {
                        sendBotKeyboardWithText(chatId, "У вас нет прав для выполнения этой команды.");
                    }
                    break;


                case "/remove_channel":
                    String link = messageText; // Предполагается, что link - это ввод пользователя
                    if (userService.getUserByUserId(userId).getUserRole().equals(UserRole.ADMIN_ROLE)) {
                        // Проверяем, существует ли канал с данной ссылкой
                        if (!channelRepository.existsByLink(link)) {
                            adminService.sendAminPanel(chatId, "Канал за такой ссылкой не найден.");
                        } else {
                            // Удаляем канал и подтверждаем удаление
                            channelService.removeChannel(link);
                            adminService.sendAminPanel(chatId, "Канал успешно удален.");
                        }
                    } else {
                        sendBotKeyboardWithText(chatId, "У вас нет доступа.");
                    }
                    break;

                case "/update_channel":
                    String linkUpdate = null;
                    String nameUpdate = null;
                    String adminUpdate = null;

                    if (userService.getUserByUserId(userId).getUserRole().equals(UserRole.ADMIN_ROLE)) {
                        // Проверяем текущее состояние администратора
                        if (adminStateHashMap.get(userId) == AdminState.NONE) {
                            linkUpdate = messageText;
                            if (channelRepository.existsByLink(linkUpdate)) {
                                adminStateHashMap.put(userId, AdminState.NAME_WRITE);
                                sendBotKeyboardWithText(chatId, "Пожалуйста, введите новое имя канала:");
                            } else {
                                adminService.sendAminPanel(chatId, "Канал с такой ссылкой не найден.");
                            }

                        } else if (adminStateHashMap.get(userId) == AdminState.NAME_WRITE) {
                            nameUpdate = messageText; // Получаем новое имя канала
                            adminStateHashMap.put(userId, AdminState.ADMIN_WRITE);
                            sendBotKeyboardWithText(chatId, "Пожалуйста, введите имя администратора:");

                        } else if (adminStateHashMap.get(userId) == AdminState.ADMIN_WRITE) {
                            adminUpdate = messageText; // Получаем имя администратора

                            // Обновляем канал
                            channelService.updateChannel(linkUpdate, nameUpdate, adminUpdate);
                            adminStateHashMap.put(userId, AdminState.NONE); // Сбрасываем состояние
                            sendBotKeyboardWithText(chatId, "Канал успешно обновлен.");
                        }
                    } else {
                        sendBotKeyboardWithText(chatId, "У вас нет доступа.");
                    }
                    break;

                case "/stats_channel":
                    // Check if the user has admin privileges
                    if (userService.getUserByUserId(userId).getUserRole().equals(UserRole.ADMIN_ROLE)) {
                        // The channel name or link should be provided in the message text
                        String channelNameOrLink = messageText.trim();

                        // Fetch the channel statistics based on the channel name or link
                        String statistics = channelService.getChannelStatistics(channelNameOrLink);

                        // Send the statistics back to the admin
                        adminService.sendAminPanel(chatId, statistics);
                    } else {
                        adminService.sendAminPanel(chatId, "У вас нет доступа.");
                    }
                    break;
                case "/all_users":
                    userService.viewAllUsers(chatId);
                    break;
                case "/user_stats":
                    userStats(chatId);
                    break;
                case "/help":
                    sendBotKeyboardWithText(chatId, HELP_TEXT);
                    break;
                case "/view_tasks":
                    sendTask(chatId);
                    break;
                case "/balance":
                    sendBotKeyboardWithText(chatId, userService.balanceText(userId));
                    break;
            }
        }
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long userId = update.getCallbackQuery().getFrom().getId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callbackData) {
                case "back":
                    // Уменьшаем индекс, чтобы вернуться к предыдущему заданию
                    int backIndex = userTaskState.getCurrentTaskIndex(chatId) - 1;
                    userTaskState.setCurrentTaskIndex(chatId, Math.max(backIndex, 0)); // Не позволяем индексу быть меньше 0
                    sendTask(chatId); // Отправляем новое задание
                    break;
                case "next":
                    // Увеличиваем индекс, чтобы перейти к следующему заданию
                    int nextIndex = userTaskState.getCurrentTaskIndex(chatId) + 1;
                    userTaskState.setCurrentTaskIndex(chatId, Math.min(nextIndex, channelService.getAllChannel().size() - 1)); // Не позволяем индексу превышать количество заданий
                    sendTask(chatId); // Отправляем новое задание
                    break;
                case "check":
                    Channel current = channelService.getAllChannel().get(userTaskState.getCurrentTaskIndex(chatId));
                    if (channelService.subscribeUserToChannel(userId, channelService.getChannelIdByUsername(current.getName()))) {
                        sendBotKeyboardWithText(chatId, "Задание выполено, на ваш баланс зачислено 5руб");
                        userService.earn(userId);
                    } else {
                        sendBotKeyboardWithText(chatId, "Задание не выполено");
                    }
                    break;
            }
        }
    }

    public void setBotCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/help", "Помощь по работе с ботом"));
        commands.add(new BotCommand("/view_tasks", "Список доступных заданий"));
        commands.add(new BotCommand("/balance", "Просмотр баланса"));

        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);

        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTask(long chatId) {
        List<Channel> channels = channelService.getAllChannel();
        int currentIndex = userTaskState.getCurrentTaskIndex(chatId);

        // Проверка на наличие доступных заданий
        if (channels.isEmpty() || currentIndex >= channels.size()) {
            sendBotKeyboardWithText(chatId, "Нет доступных заданий.");
            userTaskState.setCurrentTaskIndex(chatId, 0); // Сбросить индекс в начало
            return;
        }

        Channel currentChannel = channels.get(currentIndex);
        String taskText = String.format("Канал: %s\nЗа подписку на данный канал вы получите 5руб на баланс", currentChannel.getName());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton buttonBack = new InlineKeyboardButton("Назад");
        buttonBack.setCallbackData("back");

        InlineKeyboardButton buttonCheck = new InlineKeyboardButton("Проверить");
        buttonCheck.setCallbackData("check");

        InlineKeyboardButton buttonNext = new InlineKeyboardButton("Вперед");
        buttonNext.setCallbackData("next");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(buttonBack);
        row.add(buttonCheck);
        row.add(buttonNext);
        keyboard.add(row);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(taskText);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void sendBotKeyboardWithText(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add(new KeyboardButton("/view_tasks"));
        row1.add(new KeyboardButton("/balance"));
        row2.add(new KeyboardButton("/instruction"));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
