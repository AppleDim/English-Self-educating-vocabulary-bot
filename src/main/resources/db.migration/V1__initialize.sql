DROP DATABASE IF EXISTS engbot_db;

DROP SCHEMA IF EXISTS telegram CASCADE;

DROP TABLE IF EXISTS users CASCADE;

DROP TABLE IF EXISTS phrases CASCADE;

DROP TABLE IF EXISTS users_phrases CASCADE;

CREATE DATABASE engbot_db;

CREATE SCHEMA telegram;

CREATE TABLE telegram.users
(
    user_id         BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(255) NOT NULL,
    nickname        VARCHAR(255) NOT NULL,
    registered_date TIMESTAMP    NOT NULL,
    user_bot_state  VARCHAR(255),
    phrase_sorting_state  VARCHAR(255)
);

CREATE TABLE telegram.phrases
(
    phrase_id     BIGSERIAL PRIMARY KEY,
    phrase        VARCHAR(255) NOT NULL,
    searched_date TIMESTAMP    NOT NULL,
    count_phrases_views INT
);

CREATE TABLE telegram.users_phrases
(
    users_phrases_id BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    phrase_id        BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES telegram.users (user_id),
    CONSTRAINT fk_phrase FOREIGN KEY (phrase_id) REFERENCES telegram.phrases (phrase_id)
);