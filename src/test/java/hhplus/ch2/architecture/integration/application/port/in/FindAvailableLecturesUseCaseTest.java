package hhplus.ch2.architecture.integration.application.port.in;

import hhplus.ch2.architecture.integration.application.SpringBootTestEnvironment;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.entity.InstructorEntity;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.entity.LectureEntity;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.entity.LectureItemEntity;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.entity.LectureItemInventoryEntity;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.jpa.InstructorJpaRepository;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.jpa.LectureItemInventoryJpaRepository;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.jpa.LectureItemJpaRepository;
import hhplus.ch2.architecture.lecture.adapter.out.persistence.jpa.LectureJpaRepository;
import hhplus.ch2.architecture.lecture.application.command.FindAvailableLectureCommand;
import hhplus.ch2.architecture.lecture.application.port.in.FindAvailableLecturesUseCase;
import hhplus.ch2.architecture.lecture.application.response.LectureResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FindAvailableLecturesUseCaseTest extends SpringBootTestEnvironment {

    @Autowired
    FindAvailableLecturesUseCase sut;

    @Autowired
    InstructorJpaRepository instructorJpaRepository;

    @Autowired
    LectureJpaRepository lectureJpaRepository;

    @Autowired
    LectureItemJpaRepository lectureItemJpaRepository;

    @Autowired
    LectureItemInventoryJpaRepository lectureItemInventoryJpaRepository;

    @DisplayName("수강할 수 있는 모든 강의를 찾을 수 있다.")
    @Test
    void findAvailableLectures() {
        // given
        LocalDateTime thisSaturday = LocalDateTime.now().with(DayOfWeek.SATURDAY);

        InstructorEntity instructorEntity = buildInstructorEntity("강사1");
        instructorJpaRepository.save(instructorEntity);

        LectureEntity lectureEntity1 = buildLectureEntity("강의1", instructorEntity);
        LectureEntity lectureEntity2 = buildLectureEntity("강의2", instructorEntity);
        lectureJpaRepository.saveAll(List.of(lectureEntity1, lectureEntity2));

        LectureItemEntity lectureItemEntity1 = buildLectureItemEntity(lectureEntity1, thisSaturday, 10L);
        LectureItemEntity lectureItemEntity2 = buildLectureItemEntity(lectureEntity1, thisSaturday.plusWeeks(1), 10L);
        LectureItemEntity lectureItemEntity3 = buildLectureItemEntity(lectureEntity2, thisSaturday, 10L);
        lectureItemJpaRepository.saveAll(List.of(lectureItemEntity1, lectureItemEntity2, lectureItemEntity3));

        LectureItemInventoryEntity lectureItemInventoryEntity1 = buildLectureItemInventoryEntity(lectureItemEntity1, 10L);
        LectureItemInventoryEntity lectureItemInventoryEntity2 = buildLectureItemInventoryEntity(lectureItemEntity2, 10L);
        LectureItemInventoryEntity lectureItemInventoryEntity3 = buildLectureItemInventoryEntity(lectureItemEntity3, 10L);
        lectureItemInventoryJpaRepository.saveAll(List.of(lectureItemInventoryEntity1, lectureItemInventoryEntity2, lectureItemInventoryEntity3));

        FindAvailableLectureCommand command = new FindAvailableLectureCommand(null);

        // when
        List<LectureResponse> lectures = sut.findAvailableLectureItems(command);

        // then
        assertThat(lectures).hasSize(3)
                .extracting(l -> tuple(l.lectureId(), l.lectureTitle(), l.instructorName(), l.leftSeat(), l.capacity()))
                .containsExactlyInAnyOrder(
                        tuple(lectureItemEntity1.getId(), "강의1", "강사1", 10L, 10L),
                        tuple(lectureItemEntity2.getId(), "강의1", "강사1", 10L, 10L),
                        tuple(lectureItemEntity3.getId(), "강의2", "강사1", 10L, 10L)
                );
    }

    @DisplayName("특정 일자의 강의만 조회할 수 있다.")
    @Test
    void findAvailableLecturesWithLectureDate() {
        // given
        LocalDateTime thisSaturday = LocalDateTime.now().with(DayOfWeek.SATURDAY);

        InstructorEntity instructorEntity = buildInstructorEntity("강사1");
        instructorJpaRepository.save(instructorEntity);

        LectureEntity lectureEntity1 = buildLectureEntity("강의1", instructorEntity);
        LectureEntity lectureEntity2 = buildLectureEntity("강의2", instructorEntity);
        lectureJpaRepository.saveAll(List.of(lectureEntity1, lectureEntity2));

        LectureItemEntity thisSaturdayLectureItem1 = buildLectureItemEntity(lectureEntity1, thisSaturday, 10L);
        LectureItemEntity thisSaturdayLectureItem2 = buildLectureItemEntity(lectureEntity2, thisSaturday, 10L);
        LectureItemEntity nextSaturdayLectureItem = buildLectureItemEntity(lectureEntity1, thisSaturday.plusWeeks(1), 10L);
        lectureItemJpaRepository.saveAll(List.of(thisSaturdayLectureItem1, thisSaturdayLectureItem2, nextSaturdayLectureItem));

        LectureItemInventoryEntity lectureItemInventoryEntity1 = buildLectureItemInventoryEntity(thisSaturdayLectureItem1, 10L);
        LectureItemInventoryEntity lectureItemInventoryEntity2 = buildLectureItemInventoryEntity(thisSaturdayLectureItem2, 10L);
        LectureItemInventoryEntity lectureItemInventoryEntity3 = buildLectureItemInventoryEntity(nextSaturdayLectureItem, 10L);
        lectureItemInventoryJpaRepository.saveAll(List.of(lectureItemInventoryEntity1, lectureItemInventoryEntity2, lectureItemInventoryEntity3));

        FindAvailableLectureCommand command = new FindAvailableLectureCommand(thisSaturday.toLocalDate());

        // when
        List<LectureResponse> lectures = sut.findAvailableLectureItems(command);

        // then
        assertThat(lectures).hasSize(2)
                .extracting(l -> tuple(l.lectureId(), l.lectureTitle(), l.instructorName(), l.leftSeat(), l.capacity()))
                .containsExactlyInAnyOrder(
                        tuple(thisSaturdayLectureItem1.getId(), "강의1", "강사1", 10L, 10L),
                        tuple(thisSaturdayLectureItem2.getId(), "강의2", "강사1", 10L, 10L)
                );
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
}
