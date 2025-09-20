package com.sprint.team2.monew.domain.article.collect;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RssCollectorTest {
    private ArticleMapper mapper;
    private RssCollector collector;

    @BeforeEach
    void setUp() {
        mapper = mock(ArticleMapper.class);
        collector = new RssCollector(mapper);
    }

    private Document createFakeDoc(String titleText, String linkText, String descText) {
        Document doc = new Document("http://test.com");
        Element item = new Element("item");
        item.appendChild(new Element("title").text(titleText));
        item.appendChild(new Element("link").text(linkText));
        item.appendChild(new Element("description").text(descText));
        item.appendChild(new Element("pubDate").text("Mon, 20 Sep 2025 10:00:00 GMT"));
        doc.appendChild(item);
        return doc;
    }

    @Test
    @DisplayName("RSS 피드에서 기사 수집 성공 시 ArticleDto 리스트 반환")
    void collectShouldReturnArticles() throws Exception {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connection = mock(Connection.class);
            Document fakeDoc = createFakeDoc("테스트 기사1", "http://news.com", "요약");

            jsoupMock.when(() -> Jsoup.connect(any(String.class))).thenReturn(connection);
            when(connection.parser(any(Parser.class))).thenReturn(connection);
            when(connection.ignoreContentType(true)).thenReturn(connection);
            when(connection.get()).thenReturn(fakeDoc);

            when(mapper.toArticleDto(any(Article.class))).thenAnswer(invocation -> {
                Article article = invocation.getArgument(0);
                return new ArticleDto(UUID.randomUUID(), article.getSource().name(),
                        article.getSourceUrl(), article.getTitle(),
                        article.getPublishDate(), article.getSummary(),
                        0L, 0L, false);
            });

            // when
            List<ArticleDto> result = collector.collect("테스트");

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).title()).contains("테스트");
            assertThat(result.get(1).title()).contains("테스트");
            assertThat(result.get(2).title()).contains("테스트");

            verify(mapper, times(3)).toArticleDto(any(Article.class));
        }
    }

    @Test
    @DisplayName("키워드 필터링 적용 시 해당 키워드 포함 기사만 반환")
    void collectShouldFilterByKeyword() throws Exception {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connection = mock(Connection.class);

            Document fakeDoc = createFakeDoc("테스트 기사", "http://news.com", "요약");

            jsoupMock.when(() -> Jsoup.connect(any(String.class))).thenReturn(connection);
            when(connection.parser(any(Parser.class))).thenReturn(connection);
            when(connection.ignoreContentType(true)).thenReturn(connection);
            when(connection.get()).thenReturn(fakeDoc);

            when(mapper.toArticleDto(any(Article.class))).thenAnswer(invocation -> {
                Article article = invocation.getArgument(0);
                return new ArticleDto(UUID.randomUUID(), article.getSource().name(),
                        article.getSourceUrl(), article.getTitle(),
                        article.getPublishDate(), article.getSummary(),
                        0L, 0L, false);
            });

            // when
            List<ArticleDto> result = collector.collect("테스트");

            // then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(dto -> dto.title().contains("테스트"));
        }
    }
}