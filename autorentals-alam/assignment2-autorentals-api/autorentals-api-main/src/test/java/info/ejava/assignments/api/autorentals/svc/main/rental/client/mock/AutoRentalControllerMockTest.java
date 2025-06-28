package info.ejava.assignments.api.autorentals.svc.main.rental.client.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsAPI;
import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalController;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalExceptionAdvice;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalService;
import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@WebMvcTest  // Test slices for mock web mvc
@Import (value = {AutoRentalController.class,AutoRentalExceptionAdvice.class})
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRentalControllerMockTest {

    @Autowired
    MockMvc mockMvc;

    static DtoUtil dtoUtil;
    static AutoRentalDTOFactory autoRentalDTOFactory;
    static AutoDTO validAuto;
    static RenterDTO validRenter;

    AutoRentalListDTO autoRentals;

    @MockitoBean
    AutoRentalService autoRentalServiceMock;

    @Captor
    ArgumentCaptor<AutoRentalDTO> dtoCaptor;

    @BeforeAll
    static void setUp(){
        autoRentalDTOFactory = new AutoRentalDTOFactory();
        dtoUtil = new JsonUtil();
        validAuto = AutoDTO.builder().dailyRate(BigDecimal.valueOf(50.5))
                            .fuelType("Gasolin")
                            .location(StreetAddressDTO.builder().city("city-1")
                            .state("state-1").street("street-1").zip("zip-1").build())
                            .make("2020").model("2015").passengers(5)
                            .build();
        validRenter = RenterDTO.builder().email("valid@email.com").firstName("John").lastName("Doe")
                                .dob(LocalDate.of(1930,2,26)).build();
    }

    @BeforeEach
    void init(){
         // without ID
         // autoRentals = autoRentalDTOFactory.listBuilder().make(5,5);
         // with ID
         autoRentals = autoRentalDTOFactory.listBuilder().make(5, 5,validAuto,validRenter, AutoRentalDTOFactory.withId);
         autoRentals.setOffset(0);
         autoRentals.setLimit(0);
         autoRentals.setTotal(autoRentals.getAutoRentals().size());
         autoRentals.setKeywords("");

    }

    @Test
    void should_find_all_autoRentals() throws Exception{
        String jsonResponse = dtoUtil.marshal(autoRentals);
        log.info(" jsonResponse - {}", jsonResponse);

        BDDMockito.when(autoRentalServiceMock.searchAutoRental(any(RentalSearchParams.class),any(Pageable.class)))
                    .thenReturn(new PageImpl<AutoRentalDTO>(autoRentals.getAutoRentals(),Pageable.unpaged(),
                                    autoRentals.getAutoRentals().size()));
        URI uri =  UriComponentsBuilder.fromPath(AutoRentalsAPI.AUTO_RENTALS_PATH)
                                        .queryParam("startDate", "2025-06-22").build().toUri();
        
        log.info("uri string- {}", uri);
        mockMvc.perform(MockMvcRequestBuilders.get(uri.toString()))
                                                .andDo(MockMvcResultHandlers.print())
                                                .andExpect(MockMvcResultMatchers.status().isOk())
                                                .andExpect(MockMvcResultMatchers.content().json(jsonResponse));
        
        verify(autoRentalServiceMock, times(1)).searchAutoRental(any(RentalSearchParams.class), any(Pageable.class));    
        BDDMockito.then(autoRentalServiceMock).should(times(1)).searchAutoRental(any(RentalSearchParams.class), any(Pageable.class));
    
    }

    @Test
    void should_find_given_autoRental_by_valid_id() throws Exception{
        String jsonResponse = dtoUtil.marshal(autoRentals.getAutoRentals().get(0));
        BDDMockito.when(autoRentalServiceMock.getAutoRental(anyString())).thenReturn(autoRentals.getAutoRentals().get(0));

        mockMvc.perform(MockMvcRequestBuilders.get(AutoRentalsAPI.AUTO_RENTAL_PATH.replace("{id}", autoRentals.getAutoRentals().get(0).getId())))
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andExpect(MockMvcResultMatchers.content().json(jsonResponse));
    }

    @Test
    void should_not_find_given_autoRental_by_invalid_id() throws Exception{

        BDDMockito.when(autoRentalServiceMock.getAutoRental(anyString()))
                    .thenThrow(ClientErrorException.NotFoundException.class);
        String uri = AutoRentalsAPI.AUTO_RENTAL_PATH.replace("{id}", autoRentals.getAutoRentals().get(0).getId());
        log.info("path - {}",uri);
        mockMvc.perform(MockMvcRequestBuilders.get(uri))
                            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void should_create_new_when_autoRental_is_valid() throws Exception{
        AutoRentalDTO withID = autoRentals.getAutoRentals().get(0);
        withID.setId(null);
        AutoRentalDTO withNullID = withID;

        String json = dtoUtil.marshal(withNullID);
        //BDDMockito.when(autosServiceMock.createRenter(any(AutoDTO.class))).thenReturn(autos.getRenters().get(0));
        BDDMockito.when(autoRentalServiceMock.createAutoRental(withNullID)).thenReturn(withID);
        mockMvc.perform(MockMvcRequestBuilders.post(AutoRentalsAPI.AUTO_RENTALS_PATH)
                        .contentType("application/json")
                        .content(json))
                        .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void should_not_create_autoRental_when_autoRental_is_invalid() throws Exception{
        AutoRentalDTO withID = autoRentals.getAutoRentals().get(0);
        withID.setId(null);
        AutoRentalDTO withNullInvalidID =  withID;
        withNullInvalidID.withAmount(null);
        

        String json = dtoUtil.marshal(withNullInvalidID);
        
        BDDMockito.when(autoRentalServiceMock.createAutoRental(withNullInvalidID)).thenThrow(ClientErrorException.InvalidInputException.class);
        mockMvc.perform(MockMvcRequestBuilders.post(AutoRentalsAPI.AUTO_RENTALS_PATH)
                        .contentType("application/json")
                        .content(json))
                        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }


    @Test
    void should_update_autoRental_when_given_valid_autoRental() throws Exception {
        AutoRentalDTO autoRentalWithoutId = autoRentalDTOFactory.listBuilder().make(1, 1,validAuto,validRenter).getAutoRentals().get(0);
        autoRentalWithoutId.setRenterName("updated - "+autoRentalWithoutId.getRenterName());
        String id = "auto-123";
        autoRentalWithoutId.setId(id);
        AutoRentalDTO AutoRentalWithId = autoRentalWithoutId;
        String json = dtoUtil.marshal(AutoRentalWithId);
        BDDMockito.when(autoRentalServiceMock.updateAutoRental(id, autoRentalWithoutId)).thenReturn(AutoRentalWithId);
        String uri = AutoRentalsAPI.AUTO_RENTAL_PATH.replace("{id}", id);
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                                    .contentType("application/json")
                                    .content(json))
                                    .andExpect(MockMvcResultMatchers.status().isOk());



    }


        @Test
    void should_delete_autoRental_with_given_id() throws Exception {

        String uri = AutoRentalsAPI.AUTO_RENTAL_PATH.replace("{id}", "auto-1");
        mockMvc.perform(MockMvcRequestBuilders.delete(uri))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        verify(autoRentalServiceMock,times(1)).removeAutoRental(anyString());
        BDDMockito.then(autoRentalServiceMock).should(times(1)).removeAutoRental(anyString());
    }

    
    @Test
    void should_delete_all_autoRentals() throws Exception {

        
        mockMvc.perform(MockMvcRequestBuilders.delete(AutoRentalsAPI.AUTO_RENTALS_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        verify(autoRentalServiceMock,times(1)).removeAllAutoRental();
        BDDMockito.then(autoRentalServiceMock).should(times(1)).removeAllAutoRental();
    }
    

    @Test
    void should_check_has_autoRental() throws Exception {
        BDDMockito.when(autoRentalServiceMock.hasAutoRental(anyString())).thenReturn(true);
        String uri = AutoRentalsAPI.AUTO_RENTAL_PATH.replace("{id}", "auto-123");
        mockMvc.perform(MockMvcRequestBuilders.head(uri))
                .andExpect(MockMvcResultMatchers.status().isOk());

        BDDMockito.when(autoRentalServiceMock.hasAutoRental(anyString())).thenReturn(false);
        String uri2 = AutoRentalsAPI.AUTO_RENTAL_PATH.replace("{id}", "auto-321");
        mockMvc.perform(MockMvcRequestBuilders.head(uri2))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(autoRentalServiceMock,times(2)).hasAutoRental(anyString());
        BDDMockito.then(autoRentalServiceMock).should(times(2)).hasAutoRental(anyString());
    
    }




    
}
