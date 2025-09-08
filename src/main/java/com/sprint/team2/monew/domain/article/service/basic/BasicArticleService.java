package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.collect.NaverApiCollector;
import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.service.ArticleService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BasicArticleService implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleRepository articleRepository;
    private final NaverApiCollector naverApiCollector;

    private final InterestRepository interestRepository;

    @Override
    public void saveByInterest(UUID interestId) {

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> InterestNotFoundException.notFound(interestId));
        for (String keyword : interest.getKeywords()) {
            List<ArticleDto> articles = naverApiCollector.collect(keyword);

            for (ArticleDto dto : articles) {
                if (!articleRepository.existsBySourceUrl(dto.sourceUrl())) {
                    Article articleEntity = articleMapper.toEntity(dto);
                    articleRepository.save(articleEntity);
                }
                log.info("[Article] keyword({})로 뉴스 수집 성공", keyword);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDto> read(UUID userId, String orderBy, String direction, int limit) {

        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();

        Pageable pageable = PageRequest.of(0, limit, sort);

        Page<Article> articles = articleRepository.findAll(pageable);

        return articles.stream()
                .map(articleMapper::toArticleDto)
                .toList();
    }
}
