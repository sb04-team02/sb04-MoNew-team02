package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.collect.NaverApiCollector;
import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.service.ArticleService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestErrorCode;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicArticleService implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleRepository articleRepository;
    private final NaverApiCollector naverApiCollector;

    private final InterestRepository interestRepository;

    @Override
    @Transactional
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
}
