package info.ejava.alamnr.assignment1.testing.autorentals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import info.ejava.assignments.testing.rentals.renters.InvalidInputException;
import info.ejava.assignments.testing.rentals.renters.RenterDTO;
import info.ejava.assignments.testing.rentals.renters.RenterValidator;
import info.ejava.assignments.testing.rentals.renters.RentersService;
import info.ejava.assignments.testing.rentals.renters.RentersServiceImpl;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {RentersTestConfiguration.class},
                properties = "spring.main.allow-bean-definition-overriding=true")
//@ContextConfiguration(classes = RentersTestConfiguration.class) // do same  as above to import beans from RentersTestConfiguration.class
//@Import(RentersTestConfiguration.class) // do same  as above to import beans from RentersTestConfiguration.class
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
@ActiveProfiles("test")
public class RentersServiceMockedNTest {
    
    @Autowired
    private RentersService rentersService;
    @Autowired
    @Qualifier("valid")
    private RenterDTO validRenter;

    @Autowired
    @Qualifier("invalid")
    private RenterDTO invalidRenter;

    @MockitoBean
    private RenterValidator validatorMock;

    @Captor
    private ArgumentCaptor<RenterDTO> renterDtoCaptor;
    @Captor
    private ArgumentCaptor<Integer> intCaptor;


    @Test
    void can_create_valid_renter(){
        // given / arrange

        // configure mock
        BDDMockito.when(validatorMock.validateNewRenter(renterDtoCaptor.capture(),intCaptor.capture()))
                                    .thenReturn(List.<String>of());

        // when / act
        RenterDTO returnedValidRenterWithId = rentersService.createRenter(validRenter);

        // then /  assert
        // inspect call 
        BDDMockito.then(validatorMock).should(times(1)).validateNewRenter(any(RenterDTO.class), anyInt());
        verify(validatorMock,times(1)).validateNewRenter(any(RenterDTO.class), anyInt());

        // verify what was given to mock
        BDDAssertions.then(renterDtoCaptor.getValue().getId()).as("passed renterDTO id is null").isNull();
        BDDAssertions.then(renterDtoCaptor.getAllValues().size()).isEqualTo(1);
        log.info("minAge - {}", intCaptor.getValue());
        //BDDAssertions.then(intCaptor.getValue()).isEqualTo(12); // minAge taken from application-default.properties when no profile supplied i.e. default
        BDDAssertions.then(intCaptor.getValue()).isEqualTo(37); // minAge taken from application-test.properties when test profile supplied

        // evaluate/assert the returned result
        BDDAssertions.and.then(returnedValidRenterWithId.getId()).isNotNull();
    }

    @Test
    void rejects_invalid_renter(){
        
        // given / arrange

        // configure mock
        BDDMockito.when(validatorMock.validateNewRenter(renterDtoCaptor.capture(),intCaptor.capture()))
                                    .thenReturn(List.<String>of("renter.firstName"));

        // when / act
        Throwable ex = BDDAssertions.catchThrowableOfType(InvalidInputException.class, 
                        () -> rentersService.createRenter(invalidRenter));

        // then /  assert
        // inspect call 
        BDDMockito.then(validatorMock).should(times(1)).validateNewRenter(any(RenterDTO.class), anyInt());
        verify(validatorMock,times(1)).validateNewRenter(any(RenterDTO.class), anyInt());

        // verify what was given to mock
        BDDAssertions.then(renterDtoCaptor.getValue().getId()).as("passed renterDTO id is null").isNull();
        BDDAssertions.then(renterDtoCaptor.getAllValues().size()).isEqualTo(1);
        log.info("minAge - {}", intCaptor.getValue());
        //BDDAssertions.then(intCaptor.getValue()).isEqualTo(12); // minAge taken from application-default.properties when no profile supplied i.e. default
        BDDAssertions.then(intCaptor.getValue()).isEqualTo(37); // minAge taken from application-test.properties when test profile supplied

        // evaluate/assert the returned result
        BDDAssertions.and.then(ex.getMessage()).contains("renter.firstName");
    }

}
