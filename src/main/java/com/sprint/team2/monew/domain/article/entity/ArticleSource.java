package com.sprint.team2.monew.domain.article.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ArticleSource {
    NAVER("NAVER"),
    HANKYUNG("한국경제"),
    CHOSUN("조선일보"),
    YONHAP("연합뉴스");

    private final String displayName;

    ArticleSource(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static ArticleSource from(String value) {
        for (ArticleSource source : values()) {
            if (source.displayName.equals(value) || source.name().equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown ArticleSource: " + value);
    }
}
