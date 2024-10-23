-- Таблица для хранения каналов
CREATE TABLE channels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    link VARCHAR(255) NOT NULL UNIQUE,
    admin_name VARCHAR(255)
);

-- Таблица для хранения пользователей
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    money BIGINT DEFAULT 0,
    role VARCHAR(255) NOT NULL
);

-- Таблица для связи каналов и подписчиков (пользователей)
CREATE TABLE channel_subscribers (
    channel_id BIGINT REFERENCES channels(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (channel_id, user_id)
);
