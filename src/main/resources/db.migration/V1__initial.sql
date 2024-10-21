CREATE TABLE channel(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    link VARCHAR(255) NOT NULL UNIQUE,
    admin_name VARCHAR(255),
    subscribed BIGINT
);
CREATE TABLE user(
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    money BIGINT,
    role VARCHAR(255) NOT NULL
);