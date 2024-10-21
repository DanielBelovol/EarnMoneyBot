-- Таблица для хранения каналов
CREATE TABLE channel (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    link VARCHAR(255) NOT NULL UNIQUE,
    admin_name VARCHAR(255)
);

-- Таблица для хранения пользователей
CREATE TABLE "user" (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    money BIGINT DEFAULT 0,
    role VARCHAR(255) NOT NULL
);

-- Таблица для связи каналов и подписчиков (пользователей)
CREATE TABLE channel_subscribers (
    channel_id BIGINT REFERENCES channel(id) ON DELETE CASCADE,
    user_id VARCHAR(255) REFERENCES "user"(id) ON DELETE CASCADE,
    PRIMARY KEY (channel_id, user_id)
);
