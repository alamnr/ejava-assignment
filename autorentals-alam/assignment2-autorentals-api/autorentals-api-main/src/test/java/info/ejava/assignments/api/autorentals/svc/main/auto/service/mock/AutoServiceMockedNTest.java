package info.ejava.assignments.api.autorentals.svc.main.auto.service.mock;

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

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.svc.autos.AutosDTORepository;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.assignments.api.autorenters.svc.renters.RenterDTORepository;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.assignments.api.autorenters.svc.utils.RentersProperties;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoTestConfiguration.class,AutoRentalsAppMain.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoServiceMockedNTest {
    
    @Autowired
    private AutosService autosService;

    @Autowired @Qualifier("validAuto")
    private AutoDTO validAutoDTO;

    @Autowired @Qualifier("invalidAuto")
    private AutoDTO invalidAutoDTO;

    @MockitoBean
    private AutosDTORepository autosDTORepository;
    @MockitoBean
    private DtoValidator dtoValidator;
    

    @Captor
    private ArgumentCaptor<AutoDTO> autoDTOCaptor;
    @Captor
    private ArgumentCaptor<Integer> intCaptor;

    @Test
    void can_create_valid_auto(){
        // when / arrange

        // configure mock
        BDDMockito.when(dtoValidator.validateDto(autoDTOCaptor.capture(), intCaptor.capture()))
                        .thenReturn(List.<String>of());
        
        BDDMockito.when(autosDTORepository.save(autoDTOCaptor.capture()))
                    .thenReturn(validAutoDTO.withId("auto-123"));
        
        // when  / act
        AutoDTO returnedAutoWithId = autosService.createAuto(validAutoDTO);

        // then
        // inspect call
        verify(dtoValidator,times(1)).validateDto(any(AutoDTO.class),  anyInt());
        BDDMockito.then(dtoValidator).should(times(1)).validateDto(any((AutoDTO.class)), anyInt());
        BDDMockito.then(autosDTORepository).should(times(1)).save(any(AutoDTO.class));
        verify(autosDTORepository, times(1)).save(any(AutoDTO.class));
             

        // verify what was given to captor
        BDDAssertions.then(autoDTOCaptor.getAllValues().get(0).getId()).isNull();
        BDDAssertions.then(autoDTOCaptor.getAllValues().size()).isEqualTo(2);
        

        // assert what was returned
        BDDAssertions.then(returnedAutoWithId.getId()).isNotNull();
        BDDAssertions.then(returnedAutoWithId.getId()).isEqualTo("auto-123");
    }

    @Test
    void reject_invalid_auto() {
        // given / arrange

        // define Mockito behavior / configure mock
        BDDMockito.when(dtoValidator.validateDto(autoDTOCaptor.capture(), intCaptor.capture()))
                        .thenReturn(List.<String>of("auto.userName is empty"));
        
        // BDDMockito.when(renterDTORepository.save(autoDTOCaptor.capture()))
        //             .thenThrow(new ClientErrorException.InvalidInputException("auto.username is empty", null));

        

        // when act 
        Throwable ex = BDDAssertions.catchThrowableOfType(ClientErrorException.InvalidInputException.class, 
                    () -> autosService.createAuto(invalidAutoDTO));


        verify(dtoValidator,times(1)).validateDto(any(AutoDTO.class),  anyInt());
        BDDMockito.then(dtoValidator).should(times(1)).validateDto(any((AutoDTO.class)), anyInt());
        //BDDMockito.then(autosDTORepository).should(times(1)).save(any(AutoDTO.class));
        //verify(autosDTORepository, times(1)).save(any(AutoDTO.class));
        
        // verify what was given to captor
        BDDAssertions.then(autoDTOCaptor.getAllValues().get(0).getId()).isNull();
        BDDAssertions.then(autoDTOCaptor.getAllValues().size()).isEqualTo(1);
        


        // assert what was returned
        BDDAssertions.then(ex.getMessage()).contains("auto.userName");
        
        
    }

}
