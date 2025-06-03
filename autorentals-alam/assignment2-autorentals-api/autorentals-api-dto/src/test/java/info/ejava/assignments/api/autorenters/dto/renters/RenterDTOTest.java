package info.ejava.assignments.api.autorenters.dto.renters;

import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import lombok.extern.slf4j.Slf4j;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class RenterDTOTest {
    private DtoUtil dtoUtil = new JsonUtil();
    private RenterDTOFactory renterFactory = new RenterDTOFactory();

    private Stream<Arguments> renters() {
        return Stream.of(
                Arguments.of(renterFactory.make()),
                Arguments.of(renterFactory.make(RenterDTOFactory.withId)),
                Arguments.of(renterFactory.listBuilder().make(3,3,RenterDTOFactory.withId))
                );
    }

    @ParameterizedTest
    @MethodSource("renters")
    void can_marshal_demarshal(/*given*/Object original) {
        //when
        String payload = dtoUtil.marshal(original);
        log.info("{}", payload);
        Object copy = dtoUtil.unmarshal(payload, original.getClass());
        //then
        BDDAssertions.then(copy).isEqualTo(original);
    }

    @Test
    void replace() {
        RenterDTO renter = renterFactory.make();
        String email = renter.getEmail();
        log.info("{}", email);
        String handle = (renter.getFirstName() + "." + renter.getLastName()).toLowerCase();
        log.info("{}", email.replaceAll("(.*)@",handle + "@"));
    }


    
}
