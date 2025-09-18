package com.sprint.team2.monew.domain.article.collect;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssCollector implements Collector {

    private final ArticleMapper articleMapper;

    @Override
    public List<ArticleDto> collect(String keyword) {
        Map<ArticleSource, String> rssMap = Map.of(
                ArticleSource.HANKYUNG, "https://www.hankyung.com/feed/all-news",
                ArticleSource.CHOSUN, "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml",
                ArticleSource.YONHAP, "http://www.yonhapnewstv.co.kr/browse/feed/"
        );

        List<ArticleDto> result = new ArrayList<>();

        for (var entry : rssMap.entrySet()) {
            ArticleSource source = entry.getKey();
            String url = entry.getValue();

            try {
                Document doc = Jsoup.connect(url)
                        .parser(Parser.xmlParser())
                        .ignoreContentType(true)
                        .get();

                Elements items = doc.select("item");
                List<ArticleDto> dtos = items.stream()
                        .map(item -> {
                            String title = textOrEmpty(item, "title");
                            String link = textOrEmpty(item, "link");
                            String description = textOrEmpty(item, "description");
                            String contentStr = getTagText(item, "content:encoded");
                            String pubDateStr = textOrEmpty(item, "pubDate");
                            LocalDateTime publishDate;
                            try {
                                publishDate = ZonedDateTime.parse(pubDateStr, DateTimeFormatter.RFC_1123_DATE_TIME)
                                        .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                                        .toLocalDateTime();
                            } catch (DateTimeParseException e) {
                                publishDate = LocalDateTime.now();
                            }

                            String summary;
                            if (!description.isBlank()) {
                                summary = description;
                            } else {
                                String content = extractFirstParagragh(contentStr);
                                if (!content.isBlank()) {
                                    summary = content;
                                } else {
                                    summary = title;
                                }
                            }

                            Article article = Article.builder()
                                    .source(source)
                                    .sourceUrl(link)
                                    .title(title)
                                    .publishDate(publishDate)
                                    .summary(summary)
                                    .build();

                            return articleMapper.toArticleDto(article);
                        })
                        .filter(dto -> keyword == null || keyword.isBlank()
                                || dto.title().toLowerCase().contains(keyword.toLowerCase())
                                || dto.summary().toLowerCase().contains(keyword.toLowerCase()))
                        .toList();

                result.addAll(dtos);
                log.info("[RSS Collector] {} 기사 수집 완료: {}건", source, dtos.size());

            } catch (IOException e) {
                log.error("[RSS Collector] {} RSS 수집 실패: {}", source, e.getMessage(), e);
            }
        }
        return result;
    }

    private static String textOrEmpty(Element item, String tag) {
        Element el = item.selectFirst(tag);
        return el != null ? el.text() : "";
    }

    private static String getTagText(Element item, String tag) {
        Element el = item.getElementsByTag(tag).first();
        return el != null ? el.text() : "";
    }

    private static String extractFirstParagragh(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document doc = Jsoup.parse(html);
        Element element = doc.selectFirst("p");
        return element != null ? element.text() : "";
    }
}
