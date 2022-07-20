CREATE TABLE birthday_reminder
(
    id                       SERIAL PRIMARY KEY,
    chat_id                  INT          NOT NULL,
    reminded_user_id         VARCHAR(255) NOT NULL,
    reminded_user_first_name VARCHAR(255) NOT NULL,
    reminded_user_last_name  VARCHAR(255) DEFAULT NULL,
    next_birthday_timestamp  BIGINT       NOT NULL,
    last_updated             BIGINT       DEFAULT NULL,
    day                      INT          NOT NULL,
    month                    INT          NOT NULL,
    year                     INT          DEFAULT NULL,
    preferred_strategy       VARCHAR(255) DEFAULT NULL,
    disabled                 BOOLEAN      DEFAULT FALSE,
    deleted                  BOOLEAN      DEFAULT FALSE,
    last_notified_days       INT          DEFAULT NULL,
    UNIQUE (chat_id, reminded_user_id)
);

CREATE INDEX birthday_reminder_next_birthday_timestamp_idx ON birthday_reminder (next_birthday_timestamp);
CREATE INDEX birthday_reminder_chat_id_idx ON birthday_reminder (chat_id);

ALTER TABLE birthday_reminder OWNER TO "rastibot";


CREATE TABLE user_table
(
    id         SERIAL PRIMARY KEY,
    chat_id    INT NOT NULL UNIQUE,
    username   VARCHAR(255) DEFAULT NULL,
    first_name VARCHAR(255) DEFAULT NULL,
    last_name  VARCHAR(255) DEFAULT NULL,
    locale     VARCHAR(255) DEFAULT NULL,
    awareness  INT          DEFAULT NULL,
    inactive   BOOLEAN      DEFAULT FALSE
);

CREATE INDEX user_table_chat_id_idx ON user_table (chat_id);
CREATE INDEX user_table_awareness_idx ON user_table (awareness);
CREATE INDEX user_table_inactive_idx ON user_table (inactive);

ALTER TABLE user_table OWNER TO "rastibot";
