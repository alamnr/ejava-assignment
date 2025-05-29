package info.ejava.assignments.api.autorentals.svc.main.renter;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RenterDTORepository;
import info.ejava.assignments.api.autorenters.svc.renters.RenterService;
import info.ejava.assignments.api.autorenters.svc.utils.RenterValidator;
import info.ejava.assignments.api.autorenters.svc.utils.RentersProperties;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes =RenterTestConfiguration.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterServiceMockedNTest {
    
    @Autowired
    private RenterService renterService;
    @Autowired @Qualifier("validRenter")
    private RenterDTO validRenterDTO;

    @Autowired @Qualifier("invalidRenter")
    private RenterDTO invalidRenterDTO;

    @MockitoBean
    private RenterDTORepository renterDTORepository;
    @MockitoBean
    private RenterValidator renterValidator;
    @MockitoBean
    private RentersProperties rentersProperties;

    @Captor
    private ArgumentCaptor<RenterDTO> renterDTOCaptor;
    @Captor
    private ArgumentCaptor<Integer> intCaptor;

    @Test
    void can_create_valid_renter(){
        // when / arrange

        // configure mock
        BDDMockito.when(renterValidator.validateNewRenter(renterDTOCaptor.capture(), intCaptor.capture()))
                        .thenReturn(List.<String>of());
        BDDMockito.when(rentersProperties.getMinAge()).thenReturn(20);
        BDDMockito.when(renterDTORepository.save(renterDTOCaptor.capture()))
                    .thenReturn(validRenterDTO.withId("renter-123"));
        
        // when  / act
        RenterDTO returnedRenterWithId = renterService.createRenter(validRenterDTO);

        // then
        // inspect call
        verify(renterValidator,times(1)).validateNewRenter(any(RenterDTO.class),  anyInt());
        BDDMockito.then(renterValidator).should(times(1)).validateNewRenter(any((RenterDTO.class)), anyInt());
        BDDMockito.then(renterDTORepository).should(times(1)).save(any(RenterDTO.class));
        verify(renterDTORepository, times(1)).save(any(RenterDTO.class));
        verify(rentersProperties,times(1)).getMinAge();
        BDDMockito.then(rentersProperties).should(times(1)).getMinAge();

        // verify what was given to captor
        BDDAssertions.then(renterDTOCaptor.getAllValues().get(0).getId()).isNull();
        BDDAssertions.then(renterDTOCaptor.getAllValues().size()).isEqualTo(2);
        BDDAssertions.then(intCaptor.getAllValues().get(0)).isEqualTo(20);

        // assert what was returned
        BDDAssertions.then(returnedRenterWithId.getId()).isNotNull();
        BDDAssertions.then(returnedRenterWithId.getId()).isEqualTo("renter-123");
    }

    @Test
    void reject_invalid_renter() {
        // given / arrange

        // define Mockito behavior / configure mock
        BDDMockito.when(renterValidator.validateNewRenter(renterDTOCaptor.capture(), intCaptor.capture()))
                        .thenReturn(List.<String>of("renter.firstName is empty"));
        BDDMockito.when(rentersProperties.getMinAge()).thenReturn(20);
        // BDDMockito.when(renterDTORepository.save(renterDTOCaptor.capture()))
        //             .thenThrow(new ClientErrorException.InvalidInputException("renter.firstname is empty", null));

        

        // when act 
        Throwable ex = BDDAssertions.catchThrowableOfType(ClientErrorException.InvalidInputException.class, 
                    () -> renterService.createRenter(invalidRenterDTO));


        verify(renterValidator,times(1)).validateNewRenter(any(RenterDTO.class),  anyInt());
        BDDMockito.then(renterValidator).should(times(1)).validateNewRenter(any((RenterDTO.class)), anyInt());
        //BDDMockito.then(renterDTORepository).should(times(1)).save(any(RenterDTO.class));
        //verify(renterDTORepository, times(1)).save(any(RenterDTO.class));
        verify(rentersProperties,times(1)).getMinAge();
        BDDMockito.then(rentersProperties).should(times(1)).getMinAge();

        // verify what was given to captor
        BDDAssertions.then(renterDTOCaptor.getAllValues().get(0).getId()).isNull();
        BDDAssertions.then(renterDTOCaptor.getAllValues().size()).isEqualTo(1);
        BDDAssertions.then(intCaptor.getAllValues().get(0)).isEqualTo(20);

        // assert what was returned
        BDDAssertions.then(ex.getMessage()).contains("renter.firstName");
        
        
    }

}
