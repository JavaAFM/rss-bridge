CREATE TABLE  IF NOT EXISTS sources (
    id bigserial PRIMARY KEY,
    source_name TEXT
);

CREATE TABLE  IF NOT EXISTS news (
    id bigserial PRIMARY KEY,
    title TEXT,
    url TEXT,
    source_id BIGINT NOT NULL,
    summary TEXT,
    main_text TEXT,
    publication_date TIMESTAMP,
    CONSTRAINT FK_news_source FOREIGN KEY (source_id) REFERENCES sources (id)
);

CREATE TABLE  IF NOT EXISTS comments (
    id bigserial PRIMARY KEY,
    author TEXT,
    publication_time TEXT,
    likes INT default 0,
    content TEXT,
    news_id BIGINT,
    CONSTRAINT FK_comments_news FOREIGN KEY (news_id) REFERENCES news (id)
);

CREATE TABLE  IF NOT EXISTS main_tags (
    id bigserial PRIMARY KEY,
    group_name TEXT,
    main_tag_name TEXT
);

CREATE TABLE  IF NOT EXISTS news_tags (
    news_id BIGINT,
    tag TEXT,
    PRIMARY KEY (news_id, tag),
    CONSTRAINT FK_news_tags_news FOREIGN KEY (news_id) REFERENCES news (id)
);
