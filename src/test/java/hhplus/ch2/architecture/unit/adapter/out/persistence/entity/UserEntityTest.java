package hhplus.ch2.architecture.unit.adapter.out.persistence.entity;

import hhplus.ch2.architecture.lecture.adapter.out.persistence.entity.UserEntity;
import hhplus.ch2.architecture.lecture.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserEntityTest {

    @DisplayName("User 에서 UserEntity 로 변환할 수 있다.")
    @Test
    void fromUser() {
        // given
        User user = User.builder()
                .id(1L)
                .name("사용자1")
                .build();

        // when
        UserEntity entity = UserEntity.fromDomain(user);

        // then
        assertThat(entity.getId()).isEqualTo(user.getId());
        assertThat(entity.getName()).isEqualTo(user.getName());
    }

    @DisplayName("UserEntity 에서 User 로 변환할 수 있다.")
    @Test
    void toUser() {
        // given
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .name("사용자1")
                .build();

        // when
        User user = entity.toDomain();

        // then
        assertThat(user.getId()).isEqualTo(entity.getId());
        assertThat(user.getName()).isEqualTo(entity.getName());
    }
}
