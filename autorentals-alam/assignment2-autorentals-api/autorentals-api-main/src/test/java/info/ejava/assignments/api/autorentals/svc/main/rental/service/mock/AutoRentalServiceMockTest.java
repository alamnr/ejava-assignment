package info.ejava.assignments.api.autorentals.svc.main.rental.service.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalDTORepository;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalServiceImpl;
import info.ejava.assignments.api.autorenters.svc.autos.AutosDTORepository;
import info.ejava.assignments.api.autorenters.svc.renters.RenterDTORepository;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRentalServiceMockTest {
    
    private AutoRentalDTO autoRentalDTO;
    @Mock
    private DtoValidator validatorMock;
    @Mock 
    private AutoRentalDTORepository repo;

    @Mock 
    private AutosDTORepository autoRepository;

    @Mock 
    private RenterDTORepository renterRepository;
    

    @InjectMocks // Mockito is instantiating this implementation class for us an injecting mocks
    private AutoRentalServiceImpl autoRentalServiceImpl;

    @Captor
    ArgumentCaptor<AutoRentalDTO> dtoCaptor;
    @Captor
    ArgumentCaptor<Integer> intCaptor;

    private AutoDTO validAuto;
    private RenterDTO validRenter;

    @BeforeEach 
    void init() {
        validAuto = AutoDTO.builder().dailyRate(BigDecimal.valueOf(50.5))
                        .fuelType("Gasolin")
                        .location(StreetAddressDTO.builder().city("city-1")
                        .state("state-1").street("street-1").zip("zip-1").build())
                        .make("2020").model("2015").passengers(5)
                        .id("auto-1")
                        .build();
        validRenter = RenterDTO.builder().email("valid@email.com").id("renter-1")
                                .firstName("John").lastName("Doe")
                                .dob(LocalDate.of(1930,2,26)).build();
    }

    @Test
    void can_create_valid_autoRental() throws JsonMappingException, JsonProcessingException{
        // given / arrange 
        
        autoRentalDTO = AutoRentalDTO.builder().amount(BigDecimal.valueOf(100))
                        .autoId("auto-1").renterId("renter-1")
                        .renterAge(21).startDate(LocalDate.now()).build();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        AutoRentalDTO deepCopy = mapper.readValue(mapper.writeValueAsString(autoRentalDTO), AutoRentalDTO.class);

        // define behavior of mock during test
        BDDMockito.when(validatorMock.validateDto(dtoCaptor.capture() , intCaptor.capture()))
                .thenReturn(List.<String>of());
        
        BDDMockito.when(repo.findAllBySearchParam(any(RentalSearchParams.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<AutoRentalDTO>(List.of(), Pageable.unpaged(), 0));
        BDDMockito.when(repo.save(dtoCaptor.capture()))
                .thenReturn(deepCopy.withId("autoRental-1"));
        BDDMockito.when(autoRepository.findById(anyString())).thenReturn(Optional.of(validAuto));
        BDDMockito.when(renterRepository.findById(anyString())).thenReturn(Optional.of(validRenter));


        // when / act 
        AutoRentalDTO returnedAutoRental = autoRentalServiceImpl.createAutoRental(autoRentalDTO);
        List<String> errMsg = validatorMock.validateDto(autoRentalDTO,20);

        // then / assert - evaluate - verify

        verify(validatorMock, times(1)).validateDto(any(AutoRentalDTO.class), anyInt()) ;
        BDDMockito.then(validatorMock).should(times(1)).validateDto(any(AutoRentalDTO.class), anyInt());   // verify what was given to captor

        log.info("dtoCaptor size - {} ", dtoCaptor.getAllValues().size());
        BDDAssertions.then(dtoCaptor.getAllValues().get(0).getId()).isNull();
        log.info("intCaptor - {}", intCaptor.getAllValues().size());
        

        // verify what was returned by mock
        BDDAssertions.then(errMsg.size()).isEqualTo(0);

        BDDAssertions.then(returnedAutoRental.getId()).isNotNull();
        BDDAssertions.then(returnedAutoRental.getId()).isEqualTo("autoRental-1");


    }


    @Test
    void reject_invalid_autorental() {
        // given 
        AutoRentalDTO autoRentalDTO  = AutoRentalDTO.builder().amount(BigDecimal.valueOf(100))
                    .autoId("auto-1").renterId("renter-1").renterAge(11)
                    .startDate(LocalDate.now()).build();
        

        // define behavior of mock while test
        BDDMockito.when(validatorMock.validateDto(dtoCaptor.capture(), intCaptor.capture()))
                    .thenReturn(List.<String>of("autoRental.age must be greater than 20"));
        
        
        // when / act
        Throwable ex = BDDAssertions.catchThrowableOfType(ClientErrorException.InvalidInputException.class,
                        ()-> autoRentalServiceImpl.createAutoRental(autoRentalDTO));

        List<String> errMsg = validatorMock.validateDto(autoRentalDTO,20);
        
        // then

        // verify / inspect the method call

        verify(validatorMock,times(1)).validateDto(any(AutoRentalDTO.class), anyInt()) ;
        BDDMockito.then(validatorMock).should(times(1)).validateDto(any(AutoRentalDTO.class), anyInt());  // verify what was given to mock

        BDDAssertions.then(dtoCaptor.getAllValues().get(0).getId()).isNull();
        //BDDAssertions.then(intCaptor.getAllValues().get(0)).isEqualTo(20);

        BDDAssertions.then(errMsg.size()).isEqualTo(1);
        BDDAssertions.then(errMsg.get(0)).contains("autoRental.age must");
    }

    

}
