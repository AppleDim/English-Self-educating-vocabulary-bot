DROP DATABASE IF EXISTS engbot_db;

DROP SCHEMA IF EXISTS telegram CASCADE;

DROP TABLE IF EXISTS users CASCADE;

DROP TABLE IF EXISTS phrases CASCADE;

DROP TABLE IF EXISTS users_phrases CASCADE;

CREATE DATABASE engbot_db;

CREATE SCHEMA telegram;

CREATE TABLE telegram.users
(
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(255) NOT NULL,
    nickname        VARCHAR(255) NOT NULL,
    registered_date TIMESTAMP    NOT NULL,
    user_bot_state  VARCHAR(255)
);

CREATE TABLE telegram.phrases
(
    id            BIGSERIAL PRIMARY KEY,
    phrase        VARCHAR(255) NOT NULL,
    searched_date TIMESTAMP    NOT NULL
);

CREATE TABLE telegram.users_phrases
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    phrase_id BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES telegram.users (id),
    CONSTRAINT fk_phrase FOREIGN KEY (phrase_id) REFERENCES telegram.phrases (id)
);