package com.sprint.team2.monew.domain.interest.repository;

import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.dto.response.InterestQueryDto;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.global.config.QuerydslConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 로컬 Postgresql로 테스트
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
public class InterestRepositoryTest {
    @Autowired
    private InterestRepository interestRepository;

    @BeforeEach
    void setUp() {
        interestRepository.deleteAll();
        interestRepository.flush();
        Interest games = new Interest("게임", List.of("RPG","FPS"));
        Interest devices = new Interest("전자기기", List.of("컴퓨터","노트북","냉장고","TV"));
        interestRepository.save(games);
        interestRepository.save(devices);
    }
    @AfterEach
    void tearDown() {
        interestRepository.deleteAll();
        interestRepository.flush();
    }

    @DisplayName("검색 시 키워드 중 한개라도 일치하면 해당 키워드를 가진 관심사는 조회된다." +
            "검색어는 관심사 이름이나 키워드 중 하나라도 일치하면 전부 검색된다.")
    @Test
    void findSucceedWithKeyword() {
        // given
        Interest rpgClass = new Interest("RPG게임직업", List.of("전사","궁수","마법사"));
        interestRepository.save(rpgClass);
        String keyword = "RPG";
        String orderBy = "name";
        String direction = "DESC";
        int limit = 10;
        CursorPageRequestInterestDto request = new CursorPageRequestInterestDto(keyword, orderBy, direction, null, null, limit);

        // when
        Slice<InterestQueryDto> findInterest = interestRepository.findAllPage(request,UUID.randomUUID());

        // then
        assertThat(findInterest).hasSize(2);
        assertThat(findInterest.getContent().get(1).name()).isEqualTo("RPG게임직업");
    }
/****************************************
 *  해당 테스트는 PostgreSQL에 종속되어 있는 쿼리문이 포함되어 있어서 CI 환경(H2 Database) 테스트를 위해 주석처리 하였습니다.
 *  사용된 모듈 : pg_bigm / bigm_similarity() 유사도 검사
 *  로컬 테스트에서 동작함을 확인하였습니다.
 *
 *  사용한 이유 :
 *  유사도 80% 이상 검사하기 위해 Postgresql의 similarity를 사용.
 *  기본 제공되는 pg_trgm의 similarity는 한글 미지원
 *  한글을 지원하는 pg_bigm의 bigm_similarity 사용.
 ****************************************/
//    @DisplayName("80퍼센트 이상 유사하지 않으면 통과한다.")
//    @Test
//    void saveSucceedWhenSimilarityLessThan80Percent() {
//        // given
//        InterestRegisterRequest request = new InterestRegisterRequest("RPG게임직업", List.of("전사","궁수","마법사"));
//
//        // when
//        Boolean bool = interestRepository.existsBySimilarityNameGreaterThan80Percent(request.name());
//
//        // then
//        assertThat(bool).isFalse();
//    }
//
//    @DisplayName("관심사 이름의 기존에 있던 것 중 유사도 80%인 것이 존재하면 저장할 수 없다.")
//    @Test
//    void saveShouldFailWhenSimilarityGreaterThanOrEqualTo80Percent() {
//        // given
//        InterestRegisterRequest request = new InterestRegisterRequest("전자기",List.of("스마트폰","태블릿"));
//
//        // when
//        Boolean bool = interestRepository.existsBySimilarityNameGreaterThan80Percent(request.name());
//
//        // then
//        assertThat(bool).isTrue();
//    }
}
