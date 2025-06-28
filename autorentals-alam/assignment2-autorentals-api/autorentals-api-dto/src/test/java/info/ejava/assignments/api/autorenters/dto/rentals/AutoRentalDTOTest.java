package info.ejava.assignments.api.autorenters.dto.rentals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import lombok.extern.slf4j.Slf4j;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class AutoRentalDTOTest {
    private DtoUtil dtoUtil = new JsonUtil();
    private AutoRentalDTOFactory autoRentalDTOFactory  = new AutoRentalDTOFactory();
    final AutoDTO validAuto = AutoDTO.builder().dailyRate(BigDecimal.valueOf(50.5))
                                    .fuelType("Gasolin")
                                    .location(StreetAddressDTO.builder().city("city-1")
                                    .state("state-1").street("street-1").zip("zip-1").build())
                                    .make("2020").model("2015").passengers(5)
                                    .build();    
            
    final RenterDTO validRenter = RenterDTO.builder().email("valid@email.com")
                                            .firstName("John").lastName("Doe")
                                            .dob(LocalDate.of(1930,2,26)).build();

    private Stream<Arguments> autoRentals() {
        return Stream.of(
                Arguments.of(autoRentalDTOFactory.make(validAuto,validRenter,1)),
                Arguments.of(autoRentalDTOFactory.make(validAuto,validRenter,1,AutoRentalDTOFactory.withId)),
                Arguments.of(autoRentalDTOFactory.listBuilder().make(3,3,validAuto, validRenter,AutoRentalDTOFactory.withId))
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
