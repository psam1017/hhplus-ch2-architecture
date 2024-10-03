package hhplus.ch2.architecture.integration.application.port.in;

import hhplus.ch2.architecture.integration.application.SpringBootTestEnvironment;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.entity.*;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.jpa.*;
import hhplus.ch2.architecture.lecture.application.RegisterLectureService;
import hhplus.ch2.architecture.lecture.application.command.RegisterLectureCommand;
import hhplus.ch2.architecture.lecture.application.response.LectureRegistrationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterLectureUseCaseTest extends SpringBootTestEnvironment {

    @Autowired
    RegisterLectureService sut;

    @Autowired
    InstructorJpaRepository instructorJpaRepository;

    @Autowired
    LectureJpaRepository lectureJpaRepository;

    @Autowired
    LectureItemJpaRepository lectureItemJpaRepository;

    @Autowired
    LectureItemInventoryJpaRepository lectureItemInventoryJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    UserLectureJpaRepository userLectureJpaRepository;

    @DisplayName("사용자가 강의를 신청할 수 있다.")
    @Test
    void registerLecture() {
        // given
        LocalDateTime thisSaturday = LocalDateTime.now().with(DayOfWeek.SATURDAY);

        InstructorEntity instructorEntity = buildInstructorEntity("강사1");
        instructorJpaRepository.save(instructorEntity);

        LectureEntity lectureEntity = buildLectureEntity("강의1", instructorEntity);
        lectureJpaRepository.save(lectureEntity);

        LectureItemEntity lectureItemEntity = buildLectureItemEntity(lectureEntity, thisSaturday, 10L);
        lectureItemJpaRepository.save(lectureItemEntity);

        LectureItemInventoryEntity lectureItemInventoryEntity = buildLectureItemInventoryEntity(lectureItemEntity, 10L);
        lectureItemInventoryJpaRepository.save(lectureItemInventoryEntity);

        UserEntity userEntity = buildUserEntity("사용자1");
        userJpaRepository.save(userEntity);

        RegisterLectureCommand command = new RegisterLectureCommand(userEntity.getId(), lectureItemEntity.getId());

        // when
        LectureRegistrationResult result = sut.registerLecture(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.lectureItemId()).isEqualTo(lectureItemEntity.getId());
        assertThat(result.userId()).isEqualTo(userEntity.getId());

        Optional<UserLectureEntity> optUserLectureEntity = userLectureJpaRepository.findById(buildUserLectureId(userEntity, lectureItemEntity));
        assertThat(optUserLectureEntity).isPresent();
        UserLectureEntity userLectureEntity = optUserLectureEntity.get();
        assertThat(userLectureEntity.getUserEntity().getId()).isEqualTo(userEntity.getId());
        assertThat(userLectureEntity.getLectureItemEntity().getId()).isEqualTo(lectureItemEntity.getId());
    }

    private InstructorEntity buildInstructorEntity(String name) {
        return InstructorEntity.instructorBuilder()
                .name(name)
                .build();
    }

    private LectureEntity buildLectureEntity(String title, InstructorEntity instructorEntity) {
        return LectureEntity.builder()
                .title(title)
                .instructorEntity(instructorEntity)
                .build();
    }

    private LectureItemEntity buildLectureItemEntity(LectureEntity lectureEntity, LocalDateTime lectureDateTime, Long capacity) {
        return LectureItemEntity.builder()
                .lectureEntity(lectureEntity)
                .lectureDateTime(lectureDateTime)
                .capacity(capacity)
                .build();
    }

    private LectureItemInventoryEntity buildLectureItemInventoryEntity(LectureItemEntity lectureItemEntity, Long leftSeat) {
        return LectureItemInventoryEntity.builder()
                .lectureItemEntity(lectureItemEntity)
                .leftSeat(leftSeat)
                .build();
    }

    private UserEntity buildUserEntity(String name) {
        return UserEntity.builder()
                .name(name)
                .build();
    }

    private UserLectureEntityId buildUserLectureId(UserEntity userEntity, LectureItemEntity lectureItemEntity) {
        return UserLectureEntityId.builder()
                .userEntityId(userEntity.getId())
                .lectureItemEntityId(lectureItemEntity.getId())
                .build();
    }
}
