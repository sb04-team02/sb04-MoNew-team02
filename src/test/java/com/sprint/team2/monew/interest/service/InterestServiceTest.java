package com.sprint.team2.monew.interest.service;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.mapper.InterestMapper;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.interest.service.basic.BasicInterestService;
import com.sprint.team2.monew.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class InterestServiceTest {
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private InterestMapper interestMapper;

    @InjectMocks
    private BasicInterestService interestService;

    @BeforeEach
    void setUp() {
        Interest interest = new Interest("음악",List.of("발라드","힙합","재즈"));
        interestRepository.save(interest);
    }

    @DisplayName("관심사를 생성할 때 이름과 키워드가 제공되면 생성에 성공한다.")
    @Test
    void createInterestShouldSucceedWithNameAndKeywords() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("스포츠",List.of("축구","야구","농구"));
        Interest interest = new Interest(interestRegisterRequest);
        InterestDto interestDto = new InterestDto(UUID.randomUUID(),"스포츠", List.of("축구","야구","농구"),0L,false);
        given(interestRepository.save(interest)).willReturn(interest);
        given(interestMapper.toDto(interest)).willReturn(interestDto);

        // when
        InterestDto savedInterestDto = interestService.create(interestRegisterRequest);

        // then
        assertNotNull(savedInterestDto);
        assertEquals(savedInterestDto.name(),interest.getName());
        assertEquals(savedInterestDto.keywords(),interest.getKeywords());
    }

    @DisplayName("저장되는 데이터의 이름이 데이터베이스에 존재하는 데이터의 이름과 80% 이상 유사하지 않아야 한다.")
    @Test
    void createInterestShouldSucceedWhenNameSimilarityLessThen80Percent() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("스포츠",List.of("축구","야구","농구"));
        Interest interest = new Interest(interestRegisterRequest);
        InterestDto interestDto = new InterestDto(UUID.randomUUID(),"스포츠", List.of("축구","야구","농구"),0L,false);
        given(interestRepository.existsBySimilarityNameGreaterThan80Percent("스포츠")).willReturn(false);
        given(interestMapper.toEntity(interestRegisterRequest)).willReturn(interest);
        given(interestRepository.save(interest)).willReturn(interest);
        given(interestMapper.toDto(interest)).willReturn(interestDto);

        // when
        InterestDto savedInterestDto = interestService.create(interestRegisterRequest);

        // then
        assertNotNull(savedInterestDto);
        assertEquals(savedInterestDto.name(),interest.getName());
        assertEquals(savedInterestDto.keywords(),interest.getKeywords());
    }

    @DisplayName("데이터베이스에 존재하는 데이터와 80% 이상 유사한 데이터는 저장될 수 없습니다.")
    @Test
    void createInterestShouldFailWhenNameSimilarityGraterThanOrEqualTo80Percent() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("음악",List.of("힙합","락","팝"));
        given(interestRepository.existsBySimilarityNameGreaterThan80Percent(anyString())).willReturn(true);

        Exception exception = assertThrows(BusinessException.class, () -> interestService.create(interestRegisterRequest));
        assertEquals("비슷한 관심사가 이미 존재합니다.",exception.getMessage());
    }
}
