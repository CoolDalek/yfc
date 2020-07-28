DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    nickname   	 TEXT UNIQUE NOT NULL,
    email        TEXT UNIQUE NOT NULL,
    password	 TEXT        NOT NULL,
    is_active	 BOOLEAN DEFAULT FALSE,
    created_at	 TIMESTAMP DEFAULT now(),
    last_updated TIMESTAMP DEFAULT now()
);