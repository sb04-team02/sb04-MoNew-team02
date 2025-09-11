CREATE TABLE users
(
    id         uuid PRIMARY KEY             DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ                  DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    email      VARCHAR(100) UNIQUE NOT NULL,
    nickname   VARCHAR(50)         NOT NULL,
    password   VARCHAR(50)         NOT NULL
);

CREATE TABLE interests
(
    id                 uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ          DEFAULT CURRENT_TIMESTAMP,
    name               VARCHAR(50) NOT NULL,
    subscription_count BIGINT,
    keywords           JSON
);

CREATE TABLE subscriptions
(
    id          uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    interest_id uuid,
    user_id     uuid,
    FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE articles
(
    id            uuid PRIMARY KEY             DEFAULT uuid_generate_v4(),
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ                  DEFAULT CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMPTZ,
    source        VARCHAR(30)         NOT NULL,
    source_url    VARCHAR(255) UNIQUE NOT NULL,
    title         VARCHAR(150)        NOT NULL,
    publish_date  TIMESTAMPTZ         NOT NULL,
    summary       TEXT                NOT NULL,
    comment_count BIGINT              NOT NULL DEFAULT 0,
    view_count    BIGINT              NOT NULL DEFAULT 0,
    interest_id   uuid,
    FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE SET NULL
);

CREATE TABLE comments
(
    id         uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ          DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    content    TEXT        NOT NULL,
    like_count BIGINT      NOT NULL DEFAULT 0,
    article_id uuid,
    user_id    uuid,
    FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT comments_like_count_nonnegative CHECK ( like_count >= 0 )
);

CREATE TABLE likes
(
    id         uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id    uuid        NOT NULL,
    comment_id uuid        NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
    CONSTRAINT uk_likes_user_comment UNIQUE (user_id, comment_id)
);

CREATE TABLE notifications
(
    id            uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ          DEFAULT CURRENT_TIMESTAMP,
    confirmed     BOOLEAN     NOT NULL,
    content       TEXT,
    resource_type VARCHAR(50) NOT NULL,
    resource_id   uuid        NOT NULL,
    user_id       uuid,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE user_activities
(
    id              uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ          DEFAULT CURRENT_TIMESTAMP,
    user_id         uuid,
    subscription_id uuid,
    recent_news     uuid,
    recent_comments uuid,
    liked_comments  uuid,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions (id) ON DELETE SET NULL,
    FOREIGN KEY (recent_news) REFERENCES articles (id) ON DELETE SET NULL,
    FOREIGN KEY (recent_comments) REFERENCES comments (id) ON DELETE SET NULL,
    FOREIGN KEY (liked_comments) REFERENCES comments (id) ON DELETE SET NULL
);