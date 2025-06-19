package info.ejava.assignments.api.autorentals.svc.main.auto.service.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.svc.autos.AutoServiceImpl;
import info.ejava.assignments.api.autorenters.svc.autos.AutosDTORepository;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoServiceMockTest {
    
    private AutoDTO autoDTO;
    @Mock
    private DtoValidator validatorMock;
    @Mock 
    private AutosDTORepository repo;
    

    @InjectMocks // Mockito is instantiating this implementation class for us an injecting mocks
    private AutoServiceImpl autosServiceImpl;

    @Captor
    ArgumentCaptor<AutoDTO> dtoCaptor;
    @Captor
    ArgumentCaptor<Integer> intCaptor;

    @Test
    void can_create_valid_auto(){
        // given / arrange 
        autoDTO = AutoDTO.builder().dailyRate(BigDecimal.valueOf(50.5))
                .fuelType("Gasolin")
                .location(StreetAddressDTO.builder().city("city-1")
                .state("state-1").street("street-1").zip("zip-1").build())
                .make("2020").model("2015").passengers(5).username("Mofig")
                .build();

        // define behavior of mock during test
        BDDMockito.when(validatorMock.validateDto(dtoCaptor.capture() , intCaptor.capture()))
                    .thenReturn(List.<String>of());
        // BDDMockito.doReturn(Collections.<String>emptyList())
        //             .when(validatorMock.validateDto(dtoCaptor.capture(), intCaptor.capture()));
        
        
        BDDMockito.when(repo.save(dtoCaptor.capture()))
                .thenReturn(AutoDTO.builder().dailyRate(BigDecimal.valueOf(50.5))
                .fuelType("Gasolin")
                .location(StreetAddressDTO.builder().city("city-1")
                .state("state-1").street("street-1").zip("zip-1").build())
                .make("2020").model("2015").passengers(5).username("Mofig")
                .build().withId("auto-1"));

        // when / act 
        AutoDTO returnedAuto = autosServiceImpl.createAuto(autoDTO);
        List<String> errMsg = validatorMock.validateDto(autoDTO, 20);

        // then / assert - evaluate - verify

        verify(validatorMock, times(2)).validateDto(any(AutoDTO.class), anyInt());
        BDDMockito.then(validatorMock).should(times(2)).validateDto(any(AutoDTO.class), anyInt());


        // verify what was given to captor

        log.info("dtoCaptor size - {} ", dtoCaptor.getAllValues().size());
        BDDAssertions.then(dtoCaptor.getAllValues().get(0).getId()).isNull();
        log.info("intCaptor - {}", intCaptor.getAllValues().size());
        

        // verify what was returned by mock
        BDDAssertions.then(errMsg.size()).isEqualTo(0);

        BDDAssertions.then(returnedAuto.getId()).isNotNull();
        BDDAssertions.then(returnedAuto.getId()).isEqualTo("auto-1");


    }

    @Test
    void reject_invalid_auto() {
        // given 
        AutoDTO autoDTO  = AutoDTO.builder().build();

        // define behavior of mock while test
        BDDMockito.when(validatorMock.validateDto(dtoCaptor.capture(), intCaptor.capture()))
                    .thenReturn(List.<String>of("auto.username is null"));
        
       
        // when / act
        Throwable ex = BDDAssertions.catchThrowableOfType(ClientErrorException.InvalidInputException.class,
                             ()->autosServiceImpl.createAuto(autoDTO));

        List<String> errMsg = validatorMock.validateDto(autoDTO,0);
       
        // then

        // verify / inspect the method call

        verify(validatorMock,times(2)).validateDto(any(AutoDTO.class), anyInt());
        BDDMockito.then(validatorMock).should(times(2)).validateDto(any(AutoDTO.class), anyInt());

        // verify what was given to mock

        BDDAssertions.then(dtoCaptor.getAllValues().get(0).getId()).isNull();
        

        BDDAssertions.then(errMsg.size()).isEqualTo(1);
        BDDAssertions.then(errMsg.get(0)).contains("auto.username is null");
    }

    

}
