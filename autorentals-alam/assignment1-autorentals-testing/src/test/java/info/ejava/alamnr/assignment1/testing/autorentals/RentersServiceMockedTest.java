package info.ejava.alamnr.assignment1.testing.autorentals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.assignments.testing.rentals.renters.InvalidInputException;
import info.ejava.assignments.testing.rentals.renters.RenterDTO;
import info.ejava.assignments.testing.rentals.renters.RenterValidator;
import info.ejava.assignments.testing.rentals.renters.RentersProperties;
import info.ejava.assignments.testing.rentals.renters.RentersService;
import info.ejava.assignments.testing.rentals.renters.RentersServiceImpl;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
//@ActiveProfiles("test")
@Slf4j
public class RentersServiceMockedTest {

    //private RentersService rentersService;
    private RenterDTO validRenter;
    //private RentersProperties renterProps = new RentersProperties(25) ;

    @Mock
    private RenterValidator  validatorMock;
    @Mock
    private RentersProperties renterProps;

    @InjectMocks // Mockito is instantiating this implementation class for us an injecting mocks
    private RentersServiceImpl rentersService;

    @Captor 
    private ArgumentCaptor<RenterDTO> renterDtoCaptor;
    @Captor 
    private ArgumentCaptor<Integer> intCaptor;

    // @BeforeEach
    // void init(){
    //     renterProps = new RentersProperties(25);
    //     rentersService = new RentersServiceImpl(renterProps, validatorMock);
    // }

    @Test
    void can_create_valid_renter(){
        // given / arrange
        validRenter = RenterDTO.builder().firstName("warren").lastName("buffet").dob(LocalDate.of(1980,2,25)).build();
        // define behavior of mock during test 
        BDDMockito.when(validatorMock.validateNewRenter(renterDtoCaptor.capture(), intCaptor.capture()))
                        .thenReturn( List.<String>of());
        // BDDMockito.doReturn(Collections.<String> emptyList())
        //              .when(validatorMock.validateNewRenter(renterDtoCaptor.capture(), intCaptor.capture()));

        // when / act
        // conduct test
        RenterDTO validRenterReturned =  rentersService.createRenter(validRenter);
        List<String> errMsgs = validatorMock.validateNewRenter(validRenter, renterProps.getMinAge());
        

        // then / assert
        // evaluate result
        verify(validatorMock,times(2)).validateNewRenter(any(RenterDTO.class), anyInt()); // verify called once
        BDDMockito.then(validatorMock).should(times(2)).validateNewRenter(validRenter, renterProps.getMinAge());

        // verify what was given to mock
        log.info("renter props' age - {}", renterProps.getMinAge());
        log.info("added RenterDTO - {}", validRenterReturned);
        log.info("renter dto captors size - {}" , renterDtoCaptor.getAllValues().size());
        //BDDAssertions.assertThat(renterDtoCaptor.getValue().getId())
        BDDAssertions.assertThat(renterDtoCaptor.getAllValues().get(0).getId())
        //BDDAssertions.assertThat(renterDtoCaptor.getAllValues().get(1).getId())
                        .as("renter id is not null/empty/blank")
                        .isNull(); // .isBlank()
        
        log.info("int captors size- {}" , intCaptor.getAllValues().size());
        //BDDAssertions.assertThat(intCaptor.getValue()).as("minAge is not equal %d", renterProps.getMinAge())
        BDDAssertions.assertThat(intCaptor.getAllValues().get(0)).as("minAge is not equal %d", renterProps.getMinAge())
        //BDDAssertions.assertThat(intCaptor.getAllValues().get(1)).as("minAge is not equal %d", renterProps.getMinAge())
                        .isEqualTo(renterProps.getMinAge());
        // verify what was returned by mock

        BDDAssertions.assertThat(errMsgs.size())
                                .as("errMsg list is not empty hence size is not Zero ")
                                .isEqualTo(0);
        BDDAssertions.assertThat(validRenterReturned.getId())
                        .as("added/returned renter id is  null/empty/blank")
                        .isNotNull();
    }

    @Test
    void rejects_invalid_renter(){

        // given / arrange
        validRenter = RenterDTO.builder().firstName("").lastName("buffet").dob(LocalDate.now()).build();
        // define behavior of mock during test 
        BDDMockito.when(validatorMock.validateNewRenter(renterDtoCaptor.capture(), intCaptor.capture()))
                        .thenReturn( List.<String>of("renter.firstName is null"));
        // BDDMockito.doReturn(Collections.<String> emptyList())
        //              .when(validatorMock.validateNewRenter(renterDtoCaptor.capture(), intCaptor.capture()));

        // when / act
        // conduct test
        Throwable ex  = BDDAssertions.catchThrowableOfType(InvalidInputException.class, () -> rentersService.createRenter(validRenter));
        List<String> errMsgs = validatorMock.validateNewRenter(validRenter, renterProps.getMinAge());
        

        // then / assert
        // evaluate result
        verify(validatorMock,times(2)).validateNewRenter(any(RenterDTO.class), anyInt()); // verify called once
        BDDMockito.then(validatorMock).should(times(2)).validateNewRenter(validRenter, renterProps.getMinAge());

        // verify what was given to mock
        log.info("renter props' age - {}", renterProps.getMinAge());
        log.info("added RenterDTO - {}", ex);
        log.info("renter dto captors size - {}" , renterDtoCaptor.getAllValues().size());
        //BDDAssertions.assertThat(renterDtoCaptor.getValue().getId())
        BDDAssertions.assertThat(renterDtoCaptor.getAllValues().get(0).getId())
        //BDDAssertions.assertThat(renterDtoCaptor.getAllValues().get(1).getId())
                        .as("renter id is not null/empty/blank")
                        .isNull(); // .isBlank()
        
        log.info("int captors size- {}" , intCaptor.getAllValues().size());
        //BDDAssertions.assertThat(intCaptor.getValue()).as("minAge is not equal %d", renterProps.getMinAge())
        BDDAssertions.assertThat(intCaptor.getAllValues().get(0)).as("minAge is not equal %d", renterProps.getMinAge())
        //BDDAssertions.assertThat(intCaptor.getAllValues().get(1)).as("minAge is not equal %d", renterProps.getMinAge())
                        .isEqualTo(renterProps.getMinAge());
        // verify what was returned by mock

        BDDAssertions.assertThat(errMsgs.size())
                                .as("errMsg list is not empty hence size is not Zero ")
                                .isEqualTo(1);
        BDDAssertions.assertThat(ex.getMessage())
                        .as("doesnot contain - renter.firstName")
                        .contains("renter.firstName");


    }
    
}
