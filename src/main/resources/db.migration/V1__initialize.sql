CREATE SCHEMA IF NOT EXISTS telegram;

CREATE DATABASE engbot_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'ru_RU.UTF-8'
    LC_CTYPE = 'ru_RU.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

CREATE TABLE IF NOT EXISTS users
(
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    registered_date TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS phrases
(
    id            BIGSERIAL PRIMARY KEY,
    phrase        VARCHAR(255) NOT NULL,
    searched_date TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS users_phrases
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    phrase_id  BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_phrase FOREIGN KEY (phrase_id) REFERENCES phrases (id)
);