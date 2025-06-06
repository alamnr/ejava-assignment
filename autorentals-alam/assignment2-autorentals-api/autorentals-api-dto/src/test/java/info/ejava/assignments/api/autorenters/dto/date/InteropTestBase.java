package info.ejava.assignments.api.autorenters.dto.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.dto.MessageDTO;


public class InteropTestBase {
    
    protected static ZonedDateTime jul4Utc = ZonedDateTime.of(1776, 7, 4, 8, 2, 4, 123456789, ZoneOffset.UTC);

    protected static MessageDTO msg = new MessageDTO("http://testing", "POST", 200 , 
                                        "OK", "ok msg", "default msg", Instant.now());
    protected static RenterDTO renter = new RenterDTOFactory().make(RenterDTOFactory.withId);
    protected static RenterListDTO renters = new RenterDTOFactory().listBuilder().make(3, 3, RenterDTOFactory.withId);

    protected static ADate dates = ADate.of(jul4Utc);
    protected static ADate datesNomsecs = ADate.of(jul4Utc.withNano(0));
    protected static ADate dates5micro = ADate.of(jul4Utc.withNano(123450000));
    protected static ADate datesEST = ADate.of(ZonedDateTime.of(jul4Utc.toLocalDateTime(), ZoneId.of("EST", ZoneId.SHORT_IDS)));
    protected static ADate datesEST5micro = ADate.of(ZonedDateTime.of(jul4Utc.toLocalDateTime(), ZoneId.of("EST", ZoneId.SHORT_IDS)).withNano(123450000));
    protected static ADate dates0430micro = ADate.of(ZonedDateTime.of(jul4Utc.toLocalDateTime(), ZoneOffset.ofHoursMinutes(4, 30)));

    private static Stream<Arguments> dtos() {
        return Stream.of(
                 Arguments.of(dates),
                 Arguments.of(datesNomsecs),
                 Arguments.of(dates5micro),
                 Arguments.of(datesEST),
                 Arguments.of(datesEST5micro),
                 Arguments.of(dates0430micro),
                 Arguments.of(msg),
                 Arguments.of(renter),
                 Arguments.of(renters)
        );
    }

    protected void compareTimes(ADate request, ADate result) {
        ZonedDateTime zdtUtc = request.getZdt().withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime dateUtc = ZonedDateTime.ofInstant(result.getInstant(), ZoneOffset.UTC);
        Assertions.assertThat(zdtUtc).isEqualTo(dateUtc);
    }

}
