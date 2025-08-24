package info.ejava.assignments.api.autorentals.svc.main.rental.client.api;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.ProvidedApiAutoRenterTestConfiguration;
import info.ejava.assignments.api.autorentals.svc.main.rental.ApiTestHelper;
import info.ejava.assignments.api.autorentals.svc.main.rental.impl.ApiImplNTestConfiguration;
import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;

//TODO: activate this test early to in
@SpringBootTest(classes={
        //AutoRentalsAppMain.class,
        ProvidedApiAutoRenterTestConfiguration.class,
        ApiImplNTestConfiguration.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"test"})
//@Disabled("activate once ApiImplNTestConfiguration and ApiTestHelperImpl ready")
@Slf4j
public class MyAutoRentalsApiNTest {
    
    @Autowired
    AutoDTOFactory autoDTOFactory ;
    @Autowired
    RenterDTOFactory renterDTOFactory;
    @Autowired
    AutoRentalDTOFactory autoRentalDTOFactory;
    @Autowired
    AutosAPIClient autosAPIClient;
    @Autowired
    RentersAPIClient rentersAPIClient;
    @Autowired
    ApiTestHelper testHelper;


    AutoDTO validAutoDTO;
    RenterDTO  validRenterDTO;
    AutoRentalDTO validAutoRentalDTO;
    //TODO: remove @Nested classes below to expose/enable categories of parent tests

    //remove this class declaration to enable creation tests

    @BeforeEach
    void init(){
        delete_all();
        //given_an_autorental();
    }

    // idempotent method (whenever called same action happen, no warning from browser ) - HEAD,PUT,DELETE,GET
    // non idempotent method (browser shows warning each time called) - POST 
    // safe method  - GET, OPTION, HEAD, TRACE
    // unsafe method - POST, PUT, DELETE

    private void delete_all() {
        BDDAssertions.then(autosAPIClient.removeAllAutos().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        BDDAssertions.then(rentersAPIClient.removeAllRenters().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        BDDAssertions.then(testHelper.removeAllRentals().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }


    private AutoRentalDTO given_an_autorental() {
            AutoDTO autoDTO = autoDTOFactory.make();
            validAutoDTO = autosAPIClient.createAuto(autoDTO).getBody();
            RenterDTO renterDTO = renterDTOFactory.make();
            validRenterDTO = rentersAPIClient.createRenter(renterDTO).getBody();

            TimePeriod timePeriod = new TimePeriod(LocalDate.now(), 2);
            AutoRentalDTO autoRentalDTO = (AutoRentalDTO) testHelper.makeProposal(validAutoDTO, validRenterDTO, timePeriod);

            log.info("autoRental to save - {}", autoRentalDTO);
            ResponseEntity<AutoRentalDTO> response = testHelper.createContract(autoRentalDTO);

            return response.getBody();

    }

    // this class declaration to enable creation tests
    @Nested 
    public class new_rental {

        @Test
        void add_newAutoRental() {
            AutoDTO autoDTO = autoDTOFactory.make();
            autoDTO = autosAPIClient.createAuto(autoDTO).getBody();
            RenterDTO renterDTO = renterDTOFactory.make();
            renterDTO = rentersAPIClient.createRenter(renterDTO).getBody();

            TimePeriod timePeriod = new TimePeriod(LocalDate.now(), 2);
            AutoRentalDTO autoRentalDTO = (AutoRentalDTO) testHelper.makeProposal(autoDTO, renterDTO, timePeriod);

            ResponseEntity<AutoRentalDTO> response = testHelper.createContract(autoRentalDTO);

            Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            log.info("created autoRental- {}", response.getBody());
        }

        
    }

    // this class declaration to enable  get/modification tests
    @Nested
    public class existing_rental {
        
        @Test
        void get_autoRental() {
             AutoDTO autoDTO = autoDTOFactory.make();
            autoDTO = autosAPIClient.createAuto(autoDTO).getBody();
            RenterDTO renterDTO = renterDTOFactory.make();
            renterDTO = rentersAPIClient.createRenter(renterDTO).getBody();

            TimePeriod timePeriod = new TimePeriod(LocalDate.now(), 2);
            AutoRentalDTO autoRentalDTO = (AutoRentalDTO) testHelper.makeProposal(autoDTO, renterDTO, timePeriod);

            ResponseEntity<AutoRentalDTO> response = testHelper.createContract(autoRentalDTO);

            String requestId = response.getBody().getId();

            ResponseEntity<AutoRentalDTO>  getResponse = testHelper.getRentalById(requestId);

            BDDAssertions.assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            BDDAssertions.then(getResponse.getBody()).isEqualTo(autoRentalDTO.withId(requestId));
        }
        
        @Test
        void get_autoRentals(){
            // given
            given_an_autorental();
            Map<String,AutoRentalDTO> existingAutoRental = new HashMap<>();
            AutoRentalListDTO autoRentals = autoRentalDTOFactory.listBuilder().make(40, 40, validAutoDTO, validRenterDTO);
            for (AutoRentalDTO  autoRentalDTO : autoRentals.getAutoRentals()) {
                   ResponseEntity<AutoRentalDTO> response = testHelper.createContract(autoRentalDTO) ;
                    BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                    AutoRentalDTO addedAutoRental = response.getBody();
                    existingAutoRental.put(addedAutoRental.getId(), addedAutoRental);
            }
            // when
            BDDAssertions.assertThat(existingAutoRental).isNotEmpty();
            RentalSearchParams searchParamsWithout = new RentalSearchParams();
            RentalSearchParams searchParamsWith = RentalSearchParams.builder().pageNumber(1).pageSize(20).build();
            ResponseEntity<AutoRentalListDTO> response = testHelper.findRentals(searchParamsWithout);
            ResponseEntity<AutoRentalListDTO> responseWithOffsetAndLimit = testHelper.findRentals(searchParamsWith);
            
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            BDDAssertions.then(responseWithOffsetAndLimit.getStatusCode()).isEqualTo(HttpStatus.OK);

            AutoRentalListDTO rentalsPageWithoutOffsetAndLimit = response.getBody();
            AutoRentalListDTO rentalsPageWithOffsetAndLimit = responseWithOffsetAndLimit.getBody();

            BDDAssertions.then(rentalsPageWithoutOffsetAndLimit.getOffset()).isEqualTo(0);
            BDDAssertions.then(rentalsPageWithoutOffsetAndLimit.getLimit()).isEqualTo(0);
            BDDAssertions.then(rentalsPageWithOffsetAndLimit.getOffset()).isEqualTo(1);
            BDDAssertions.then(rentalsPageWithOffsetAndLimit.getLimit()).isEqualTo(20);

            BDDAssertions.then(rentalsPageWithoutOffsetAndLimit.getCount()).isEqualTo(existingAutoRental.size()+1);
            BDDAssertions.then(rentalsPageWithOffsetAndLimit.getCount()).isEqualTo(20);
            
            for (AutoRentalDTO q: rentalsPageWithoutOffsetAndLimit.getAutoRentals()) {
                if(existingAutoRental.containsKey(q.getId())){
                    BDDAssertions.then(existingAutoRental.remove(q.getId())).isNotNull();
                }
            }
            BDDAssertions.then(existingAutoRental).isEmpty();

        }
        @Test
        void update_an_existing_autoRental_whose_timePeriod_is_not_overlapped( ){

            // given an existing autorental
            AutoRentalDTO existingAutoRentalDTO = given_an_autorental();
            String autoRentalId = existingAutoRentalDTO.getId();
            log.info("startDate - {} , endDate - {}", existingAutoRentalDTO.getStartDate(), existingAutoRentalDTO.getEndDate());
            AutoRentalDTO updatedAutoRental = existingAutoRentalDTO
                                            .withStartDate(existingAutoRentalDTO.getStartDate().plusDays(1))
                                            .withEndDate(existingAutoRentalDTO.getEndDate().plusDays(1))
                                            .withId(autoRentalId);

            log.info("startDate - {} , endDate - {}", existingAutoRentalDTO.getStartDate(), existingAutoRentalDTO.getEndDate());

            // when - updating existing autoRental

            ResponseEntity<Void> response = testHelper.modifyContract(updatedAutoRental);

            // then - evaluate / assert

            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            ResponseEntity<AutoRentalDTO> getUpdatedAutoRental = testHelper.getRentalById(autoRentalId);

            BDDAssertions.then(getUpdatedAutoRental.getStatusCode()).isEqualTo(HttpStatus.OK);

            BDDAssertions.then(getUpdatedAutoRental.getBody()).isNotEqualTo(existingAutoRentalDTO);
        }
    
    }

    // this class declaration to enable query tests
    @Nested 
    public class can_query_to {
        @Test
        void searchAutoRentals(){

            
            // given
            // given - an existing autoRental
            AutoRentalDTO existingAutoRental = given_an_autorental();
            
            final TimePeriod timePeriod = new TimePeriod(existingAutoRental.getStartDate(), 
                                    existingAutoRental.getEndDate() != null ? existingAutoRental.getEndDate() : existingAutoRental.getStartDate());
            RentalSearchParams searchParams = RentalSearchParams.builder()
                                                    .autoId(existingAutoRental.getAutoId())
                                                    .renterId(existingAutoRental.getRenterId())
                                                    .timePeriod(timePeriod)
                                                    //.pageNumber(1)
                                                    //.pageSize(10)
                                                    .build();
            
            // when 
            ResponseEntity<AutoRentalListDTO> response = testHelper.findRentals(searchParams);

            //then - page of results returned
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            AutoRentalListDTO returnedAutoRentals = response.getBody();
            log.info("returned autorentals - {}", returnedAutoRentals);
                                        
        }
    }
    
    // this class declaration to enable query tests
    @Nested
    public class date_query {

    }

    // this class declaration to enable to tests for rule judge in future
    @Nested
    public class future_rentals {

    }
}
