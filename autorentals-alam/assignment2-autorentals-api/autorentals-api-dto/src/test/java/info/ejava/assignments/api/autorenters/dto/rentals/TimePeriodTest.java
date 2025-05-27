package info.ejava.assignments.api.autorenters.dto.rentals;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
public class TimePeriodTest {

    @Test
    void can_create_by_startDate_endDate_inclusive() {
        //given
        LocalDate startDate = LocalDate.now();
        //timePeriod is inclusive
        LocalDate endDate = LocalDate.now().plusDays(7).minusDays(1);
        //when
        TimePeriod timePeriod = new TimePeriod(startDate, endDate);
        //then
        then(timePeriod.getStartDate()).isEqualTo(startDate);
        then(timePeriod.getEndDate()).isEqualTo(endDate);
        then(timePeriod.getDays()).isEqualTo(7);
        then(timePeriod.getPeriod()).isEqualTo(Period.ofDays(7));
    }

    @Test
    void can_create_by_startDate_inclusive() {
        //given
        LocalDate startDate = LocalDate.now();
        //when
        TimePeriod timePeriod = new TimePeriod(startDate);
        //then
        then(timePeriod.getStartDate()).isEqualTo(startDate);
        then(timePeriod.getEndDate()).isEqualTo(startDate);
        then(timePeriod.getDays()).isEqualTo(1);
        then(timePeriod.getPeriod()).isEqualTo(Period.ofDays(1));
    }

    @Test
    void can_create_by_startDate_plus_days() {
        //given
        LocalDate startDate = LocalDate.now();
        //when
        TimePeriod timePeriod = new TimePeriod(startDate, 7);
        //then
        then(timePeriod.getStartDate()).isEqualTo(startDate);
        then(timePeriod.getEndDate()).isEqualTo(startDate.plusDays(7).minusDays(1));
        then(timePeriod.getDays()).isEqualTo(7);
        then(timePeriod.getPeriod()).isEqualTo(Period.ofDays(7));
    }

    @Test
    void can_create_static() {
        //verify
        then(TimePeriod.create(null, null)).isNull();

        //verify
        LocalDate date = LocalDate.now();
        //when
        TimePeriod timePeriod = TimePeriod.create(null, date);
        //then
        then(timePeriod.getStartDate()).isEqualTo(date);
        then(timePeriod.getDays()).isEqualTo(1);
        then(timePeriod.getStartDate()).isEqualTo(timePeriod.getEndDate());
    }

    @Test
    void can_slide_days() {
        //given
        TimePeriod timePeriod = new TimePeriod(LocalDate.now(),7);
        //when
        TimePeriod next = timePeriod.slide(7);
        //then
        then(next.getStartDate()).isEqualTo(timePeriod.getStartDate().plusDays(7));
        then(next.getStartDate()).isEqualTo(timePeriod.getEndDate().plusDays(1));
        then(next.getDays()).isEqualTo(7);
        then(next.getPeriod()).as(()->String.format("%s is %d days",next, next.getDays())).isEqualTo(Period.ofDays(7));
    }

    @Test
    void can_slide_period() {
        //given
        TimePeriod timePeriod = new TimePeriod(LocalDate.now(),7);
        //when
        TimePeriod next = timePeriod.slide(Period.ofWeeks(8));
        //then
        then(next.getStartDate()).isEqualTo(timePeriod.getStartDate().plusWeeks(8));
        log.info("{} is {} days", timePeriod, timePeriod.getDays());
        log.info("{} is {} days", next, next.getDays());
        then(next.getPeriod()).as(()->String.format("%s is %d days",next, next.getDays())).isEqualTo(Period.ofDays(7));
    }

    @Test
    void prev_is_before() {
        //given
        TimePeriod original = new TimePeriod(LocalDate.now(), 7);
        int originalSpan = original.getDays();
        //when
        TimePeriod prev = original.prev();
        //then
        then(prev.getDays()).isEqualTo(originalSpan);
        then(prev.getEndDate()).isBefore(original.getStartDate());
        then(Period.between(prev.getEndDate(), original.getStartDate()).getDays()).isEqualTo(1);

        then(original.isOverlap(prev)).isFalse();
    }

    @Test
    void next_is_after() {
        //given
        TimePeriod original = new TimePeriod(LocalDate.now(), 7);
        int originalSpan = original.getDays();
        //when
        TimePeriod next = original.next();
        //then
        then(next.getDays()).isEqualTo(originalSpan);
        then(next.getStartDate()).isAfter(original.getEndDate());
        then(Period.between(original.getEndDate(), next.getStartDate())).isEqualTo(Period.ofDays(1));

        then(original.isOverlap(next)).isFalse();
    }

    @Test
    void is_overlap() {
        //given
        TimePeriod original = new TimePeriod(LocalDate.now(), 7);
        int originalSpan = original.getDays();
        //when
        TimePeriod next = original.slide(original.getPeriod().minusDays(3));
        //then
        then(next.getDays()).isEqualTo(originalSpan);
        then(next.getStartDate()).isBefore(original.getEndDate());
        then(next.getEndDate()).isAfter(original.getStartDate());

        then(original.isOverlap(next)).isTrue();
    }

    @Test
    void next_is_not_overlap() {
        //given
        TimePeriod original = new TimePeriod(LocalDate.now(), 7);
        int originalSpan = original.getDays();
        //when
        TimePeriod next = original.next();
        //then
        then(next.getDays()).isEqualTo(originalSpan);
        then(next.getStartDate()).isAfter(original.getEndDate());
        then(Period.between(next.getStartDate(), original.getEndDate()).getDays()).isEqualTo(-1);

        then(original.isOverlap(next)).isFalse();
    }


    @Test
    void isBetween() {
        TimePeriod timePeriod = new TimePeriod(LocalDate.now(), 7);

        then(timePeriod.isBetween(LocalDate.now().plusDays(-1))).isFalse();
        for (int i=0; i<7; i++) {
            then(timePeriod.isBetween(LocalDate.now().plusDays(i))).isTrue();
        }
        then(timePeriod.isBetween(LocalDate.now().plusDays(8))).isFalse();
    }

    @Test
    void null_not_between() {
        TimePeriod timePeriod = new TimePeriod(LocalDate.now(),7);
        //when
        then(timePeriod.isBetween(null)).isFalse();
    }

    @Test
    void null_not_overlap() {
        TimePeriod timePeriod = new TimePeriod(LocalDate.now(),7);
        //when
        then(timePeriod.isOverlap(null)).isFalse();
    }

    static Stream<Arguments> can_compare() {
        LocalDate today = LocalDate.now();
        LocalDate past = today.minusDays(14);
        LocalDate future = today.plusDays(14);
        return Stream.of(
                Arguments.of(new TimePeriod(past,3),new TimePeriod(past,3), 0),
                Arguments.of(new TimePeriod(past,3), new TimePeriod(future), -1),
                //overlap
                Arguments.of(new TimePeriod(past,today.plusDays(3)),
                        new TimePeriod(today.minusDays(3),future), 0)
        );
    }

    @ParameterizedTest
    @MethodSource
    void can_compare(TimePeriod lhs, TimePeriod rhs, int result) {
        //verify
        then(lhs.compareTo(rhs)).isEqualTo(result);
        then(rhs.compareTo(lhs)).isEqualTo(result*-1);
    }
}
