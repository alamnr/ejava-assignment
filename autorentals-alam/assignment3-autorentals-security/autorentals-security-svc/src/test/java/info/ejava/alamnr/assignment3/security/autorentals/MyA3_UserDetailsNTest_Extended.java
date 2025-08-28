package info.ejava.alamnr.assignment3.security.autorentals;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import info.ejava.assignments.api.autorentals.svc.main.rental.ApiTestHelper;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.security.autorenters.svc.Accounts;
import info.ejava.assignments.security.autorenters.svc.rentals.A3_UserDetailsNTest;
import info.ejava.examples.common.web.ServerConfig;
import jakarta.servlet.FilterChain;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes= {
        AutoRentalsSecurityApp.class,
        SecurityTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test","userdetails"})
@Slf4j
@DisplayName("Part A3: User Details")
//@Disabled

public class MyA3_UserDetailsNTest_Extended {

    @Autowired
    @Qualifier("usernameMap")
    private Map<String, RestTemplate> authnUsers;
    
    @Autowired
    private RestTemplate anonymousUser;

    @Autowired
    private Accounts accounts;

    @Autowired
    private AutoDTOFactory autoFactory;
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired
    private AutosAPIClient autosAPIClient;
    @Autowired
    private RentersAPIClient rentersAPIClient;
    @Autowired
    private ApiTestHelper<RentalDTO> testHelper;
    @Autowired
    private ServerConfig serverConfig;
    private URI whoAmIUrl;
    private @Autowired Environment env;
    @Autowired (required = false)
    List<PasswordEncoder> passwordEncoder;
    @Autowired(required = false)
    List<UserDetailsService> userdetails;

    

    @BeforeEach
    void init(){
        Assumptions.assumeFalse(getClass().equals(A3_UserDetailsNTest.class),"should only run for derived class ");
        BDDAssumptions.given(userdetails).as("no user details found for accounts").isNotEmpty();
        BDDAssumptions.given(passwordEncoder).as("no password encoder found").isNotEmpty();
        BDDAssumptions.given(env.getActiveProfiles()).as("missing required profile").contains("userdetails");

        whoAmIUrl = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path("/api/whoAmI").build().toUri();

        log.info("whoAmI url - {}", whoAmIUrl);
    }

    @Test
    void context(){
        log.info("**************************************************** account - {}", accounts);
        log.info("**************************************************** authnUsers - {}", authnUsers);
        log.info("************************** userdetails - {}", userdetails);
        
    }

        @Test
        void valid_credentials_can_authenticate(){

            // given
             
            String username = authnUsers.keySet().iterator().next(); 
            log.info("**************************************************** user - {} ", username);
            RestTemplate authnUser = authnUsers.get(username);
          
            RequestEntity<Void>  request = RequestEntity.get(whoAmIUrl).build();
            // when
            ResponseEntity<String> response = authnUser.exchange(request, String.class);
            String body = response.getBody();
            // then
            log.info("*************** response body - {} ", body);
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            BDDAssertions.then(body).isEqualTo(username);
        }
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class with_identities {

        Stream<Arguments> identities() {
            return authnUsers.entrySet().stream()
                                    .map(au->Arguments.of(au.getValue(), au.getKey()));
        }

        @ParameterizedTest(name = "{index} {1} can authenticate")
        @MethodSource("identities")
        void valid_credentials_can_authenticate(RestTemplate authnUser, String username){
            // given
            RequestEntity<Void>  request = RequestEntity.get(whoAmIUrl).build();
            // when
            ResponseEntity<String> response = authnUser.exchange(request, String.class);
            String body = response.getBody();
            // then
            log.info("*************** user - {} ", body);
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            BDDAssertions.then(body).isEqualTo(username);
        }
        
        @ParameterizedTest(name="{index} {1} can create auto")
        @MethodSource("identities")
        void valid_credentials_can_create_auto(RestTemplate authnUser, String username) {
            // given
            AutoDTO validAuto = autoFactory.make();
            AutosAPI autosClient = autosAPIClient.withRestTemplate(authnUser);
            // when
            ResponseEntity<AutoDTO> response = autosClient.createAuto(validAuto);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);        
        }


    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class create_renter {

        @BeforeEach
        void cleanUp(){
            // we can only have 1 renter per username - cleanup
            RestTemplate authnUser = authnUsers.values().iterator().next();
            rentersAPIClient.withRestTemplate(authnUser).removeAllRenters();
        }

        Stream<Arguments> identities() {
            return authnUsers.entrySet().stream().map(au->Arguments.of(au.getValue(),au.getKey()));
        }

        @ParameterizedTest(name = "{index} {1} can create renter")
        @MethodSource("identities")
        void valid_credentials_can_create_renter(RestTemplate authnUser, String username) {
            // given
            RenterDTO validRenter = renterFactory.make();
            RentersAPI rentersClientt = rentersAPIClient.withRestTemplate(authnUser);
            // when
            ResponseEntity<RenterDTO> response = rentersClientt.createRenter(validRenter);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }


    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class create_auto {

        Stream<Arguments> identities(){
            return authnUsers.entrySet().stream().map(au->Arguments.of(au.getValue(),au.getKey()));
        }

        @ParameterizedTest(name = "{index} {1} can create auto")
        @MethodSource("identities")
        void valid_credentials_can_create_auto(RestTemplate authnUser, String username){
            // given
            AutoDTO validAuto = autoFactory.make();
            AutosAPIClient autosClient = autosAPIClient.withRestTemplate(authnUser);
            // when
            ResponseEntity<AutoDTO> response = autosClient.createAuto(validAuto);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class create_rental {
        @AfterAll
        void cleanUp(){
            // we can only have 1 renter per username -- cleanup
            RestTemplate authnUser = authnUsers.values().iterator().next();
            try {
                testHelper.withRestTemplate(authnUser).removeAllRentals();
            } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized ex) {
                Assertions.fail("authenticated user failed to delete all in profile that does not have roles; " +
                        "if this worked eariler, make sure you have applied authorization checks in a way that " +
                        "will not be active during profile(s): " + Arrays.toString(env.getActiveProfiles()));
            }
            rentersAPIClient.withRestTemplate(authnUser).removeAllRenters();
            autosAPIClient.withRestTemplate(authnUser).removeAllAutos();
        }

        Stream<Arguments> identities(){
            return authnUsers.entrySet().stream().map(au->Arguments.of(au.getValue(),au.getKey()));
        }
        @ParameterizedTest(name = "{index} {1} can create contract")
        @MethodSource("identities")
        void valid_credential_can_create_rentals(RestTemplate authnUser, String username){

            // given
            AutosAPI autosClient = autosAPIClient.withRestTemplate(authnUser);
            RentersAPI rentersClient = rentersAPIClient.withRestTemplate(authnUser);
            ApiTestHelper<RentalDTO> rentalsHelper = testHelper.withRestTemplate(authnUser);

            AutoDTO auto = autosClient.createAuto(autoFactory.make()).getBody();
            RenterDTO renter = rentersClient.createRenter(renterFactory.make()).getBody();
            TimePeriod timePeriod = new TimePeriod(LocalDate.now(), 2);
            AutoRentalDTO autoRentalDTO = (AutoRentalDTO) rentalsHelper.makeProposal(auto, renter, timePeriod);
            // when
            ResponseEntity<RentalDTO> response = rentalsHelper.createContract(autoRentalDTO);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

}   
