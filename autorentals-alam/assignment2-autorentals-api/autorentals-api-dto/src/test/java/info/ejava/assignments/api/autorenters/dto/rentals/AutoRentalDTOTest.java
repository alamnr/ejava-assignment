package info.ejava.assignments.api.autorenters.dto.rentals;

import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import lombok.extern.slf4j.Slf4j;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class AutoRentalDTOTest {
    private DtoUtil dtoUtil = new JsonUtil();
    private AutoRentalDTOFactory autoRentalDTOFactory  = new AutoRentalDTOFactory();
    

    private Stream<Arguments> autoRentals() {
        return Stream.of(
                Arguments.of(autoRentalDTOFactory.make()),
                Arguments.of(autoRentalDTOFactory.make(AutoRentalDTOFactory.withId)),
                Arguments.of(autoRentalDTOFactory.listBuilder().make(3,3,AutoRentalDTOFactory.withId))
                );
    }

    @ParameterizedTest
    @MethodSource("autoRentals")
    void can_marshal_demarshal(/*given*/Object original) {
        log.info("Object Original - {}", original);
        //when
        String payload = dtoUtil.marshal(original);
        log.info("payload - {}", payload);
        Object copy = dtoUtil.unmarshal(payload, original.getClass());
        
        log.info("Object from payload - {}", copy);
        //then
        BDDAssertions.then(copy).isEqualTo(original);
        
    }

        
}
