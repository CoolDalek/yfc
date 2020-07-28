DROP TABLE IF EXISTS token;
CREATE TABLE token
(
    user_id   BIGINT REFERENCES users ON DELETE CASCADE,
    body      TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);