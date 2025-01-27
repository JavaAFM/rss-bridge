CREATE TABLE  IF NOT EXISTS sources (
    id bigserial PRIMARY KEY,
    source_name VARCHAR(255)
);

CREATE TABLE  IF NOT EXISTS news (
    id bigserial PRIMARY KEY,
    title VARCHAR(255),
    url VARCHAR(255),
    source_id BIGINT NOT NULL,
    image_url VARCHAR(255),
    summary TEXT,
    main_text TEXT,
    publication_date TIMESTAMP,
    CONSTRAINT FK_news_source FOREIGN KEY (source_id) REFERENCES sources (id)
);

CREATE TABLE  IF NOT EXISTS comments (
    id bigserial PRIMARY KEY,
    author VARCHAR(255),
    publication_time VARCHAR(255),
    likes INT default 0,
    content TEXT,
    news_id BIGINT,
    CONSTRAINT FK_comments_news FOREIGN KEY (news_id) REFERENCES news (id)
);

CREATE TABLE  IF NOT EXISTS main_tags (
    id bigserial PRIMARY KEY,
    group_name VARCHAR(255),
    main_tag_name VARCHAR(255)
);

CREATE TABLE  IF NOT EXISTS news_tags (
    news_id BIGINT,
    tag VARCHAR(255),
    PRIMARY KEY (news_id, tag),
    CONSTRAINT FK_news_tags_news FOREIGN KEY (news_id) REFERENCES news (id)
);
