CREATE TABLE IF NOT EXISTS loan
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id        BIGINT    NOT NULL,
    member_id      BIGINT    NOT NULL,
    checked_out_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS book
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    title     VARCHAR NOT NULL
);

