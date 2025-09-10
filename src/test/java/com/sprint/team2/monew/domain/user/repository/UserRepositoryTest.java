package com.sprint.team2.monew.domain.user.repository;

import com.sprint.team2.monew.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("해당 이메일을 가진 사용자가 존재하면 true")
    void existsByEmailExistingEmailReturnsTrue() {
        // given
        String existingEmail = "existingEmail@email.com";
        User user = new User(existingEmail, "test1234", "test");
        userRepository.save(user);
        
        // when
        boolean isExists = userRepository.existsByEmail(existingEmail);

        // then
        assertThat(isExists).isTrue();
    }

    @Test
    @DisplayName("해당 이메일을 가진 사용자가 존재하지 않으면 false")
    void existsByEmailNonExistingEmailReturnsFalse() {
        // given
        String nonExistingEmail = "nonExistingEmail@email.com";

        // when
        boolean isExists = userRepository.existsByEmail(nonExistingEmail);

        // then
        assertThat(isExists).isFalse();
    }

    @Test
    @DisplayName("이메일로 사용자를 찾을 수 있다")
    void findByEmailExistingEmailReturnsUser() {
        // given
        String existingEmail = "existingEmail@email.com";
        User user = new User(existingEmail, "test1234", "test");
        userRepository.save(user);

        // when
        Optional<User> findUser = userRepository.findByEmail(existingEmail);

        // then
        assertThat(findUser.isPresent()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 검색하면 빈 Optional 반환")
    void findByEmailNonExistingEmailReturnsEmptyOptional() {
        // given
        String nonExistingEmail = "nonExistingEmail@email.com";

        // when
        Optional<User> foundUser = userRepository.findByEmail(nonExistingEmail);

        // then
        assertThat(foundUser.isPresent()).isFalse();
    }


    @Test
    @DisplayName("threshold보다 더 과거의 deletedAt을 가진 사용자 목록 반환")
    void findAllByDeletedAtBeforeExistingDeletedAtBeforeThreshold() {
        // given
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);

        List<User> users = List.of(new User("test1@email.com", "test1234", "test1"),
                new User("test2@email.com", "test1234", "test2"));

        for(User user : users) {
            user.setDeletedAt(threshold.minusHours(1));
        }

        userRepository.saveAll(users);

        // 영속성 컨텍스트 초기화
        entityManager.flush();
        entityManager.clear();

        // when
        List<User> foundUsers = userRepository.findAllByDeletedAtBefore(threshold);

        // then
        assertThat(foundUsers.size()).isEqualTo(2);
        assertThat(users).extracting("nickname").containsExactlyInAnyOrder("test1", "test2");
    }

    @Test
    @DisplayName("threshold보다 더 과거의 deletedAt을 가진 사용자가 없으면 빈 List 반환")
    void findAllByDeletedAtBeforeNonExistingDeletedAtBeforeThreshold() {
        // given
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);

        List<User> users = List.of(new User("test1@email.com", "test1234", "test1"),
                new User("test2@email.com", "test1234", "test2"));

        for(User user : users) {
            user.setDeletedAt(LocalDateTime.now());
        }

        userRepository.saveAll(users);

        // 영속성 컨텍스트 초기화
        entityManager.flush();
        entityManager.clear();

        // when
        List<User> foundUsers = userRepository.findAllByDeletedAtBefore(threshold);

        // then
        assertThat(foundUsers).isEmpty();
    }
}