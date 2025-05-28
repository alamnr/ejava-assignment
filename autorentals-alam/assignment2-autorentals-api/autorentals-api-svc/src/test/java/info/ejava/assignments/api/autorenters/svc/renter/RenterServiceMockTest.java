package info.ejava.assignments.api.autorenters.svc.renter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
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

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RenterDTORepository;
import info.ejava.assignments.api.autorenters.svc.renters.RenterServiceImpl;
import info.ejava.assignments.api.autorenters.svc.utils.RenterValidator;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterServiceMockTest {
    
    private RenterDTO renterDTO;
    @Mock
    private RenterValidator validatorMock;
    @Mock 
    private RenterDTORepository repo;

    @InjectMocks // Mockito is instantiating this implementation class for us an injecting mocks
    private RenterServiceImpl renterServiceImpl;

    @Captor
    ArgumentCaptor<RenterDTO> dtoCaptor;
    @Captor
    ArgumentCaptor<Integer> intCaptor;

    @Test
    void can_create_valid_renter(){
        // given / arrange 
        renterDTO = RenterDTO.builder().firstName("John")
                        .lastName("Doe").dob(LocalDate.of(1380, 5, 26)).build();

        // define behavior of mock during test
        BDDMockito.when(validatorMock.validateNewRenter(dtoCaptor.capture() , intCaptor.capture()))
                    .thenReturn(List.<String>of());
        // BDDMockito.doReturn(Collections.<String>emptyList())
        //             .when(validatorMock.validateNewRenter(dtoCaptor.capture(), intCaptor.capture()));

        
        BDDMockito.when(repo.save(dtoCaptor.capture()))
                .thenReturn(RenterDTO.builder().dob(renterDTO.getDob()).email(renterDTO.getEmail()).firstName(renterDTO.getFirstName())
                    .lastName(renterDTO.getLastName()).build().withId("renter-1"));

        // when / act 
        RenterDTO returnedRenter = renterServiceImpl.createRenter(renterDTO);
        List<String> errMsg = validatorMock.validateNewRenter(renterDTO, 20);

        // then / assert - evaluate - verify

        verify(validatorMock, times(2)).validateNewRenter(any(RenterDTO.class), anyInt());
        BDDMockito.then(validatorMock).should(times(2)).validateNewRenter(any(RenterDTO.class), anyInt());


        // verify what was given to captor

        log.info("dtoCaptor size - {} ", dtoCaptor.getAllValues().size());
        BDDAssertions.then(dtoCaptor.getAllValues().get(0).getId()).isNull();
        log.info("intCaptor - {}", intCaptor.getAllValues().size());
        BDDAssertions.then(intCaptor.getAllValues().get(0)).isEqualTo(20);

        // verify what was returned by mock
        BDDAssertions.then(errMsg.size()).isEqualTo(0);

        BDDAssertions.then(returnedRenter.getId()).isNotNull();
        BDDAssertions.then(returnedRenter.getId()).isEqualTo("renter-1");


    }

}
