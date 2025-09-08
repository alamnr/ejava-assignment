package info.ejava.assignments.security.autorenters.svc.rentals;

import static org.mockito.ArgumentMatchers.isNull;

import java.net.URI;
import java.time.LocalDate;

import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;

//@SpringBootTest(classes= { ...
//    },
//    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles({"test", "authenticated-access"})
//@DisplayName("Part A2: Authenticated Access")
@Slf4j
public class A2_AuthenticatedAccessNTest extends A1_AnonymousAccessNTest 
{
   
    @Autowired
    private ApiTestHelper<RentalDTO> testHelper;
    @Autowired
    AutosAPIClient autosAPIClient;
    @Autowired
    RentersAPIClient rentersAPIClient;
    @Autowired
    AutoDTOFactory autoDTOFactory;
    @Autowired
    RenterDTOFactory renterDTOFactory;
    @Autowired
    RestTemplate authnUser;
    @Autowired
    RestTemplate badUser;
    @Autowired
    String authnUsername;
    @Autowired
    RestTemplate anonymousUser;
    @Autowired
    String anonymousUsername;
    @Autowired
    ServerConfig serverConfig;
    @Autowired Environment env;
    @Autowired(required = false)
    SecurityFilterChain filterChain;

    
    @BeforeEach
    void verify(){
        Assumptions.assumeFalse(getClass().equals(A2_AuthenticatedAccessNTest.class),"should only run for derived class");
        BDDAssumptions.given(env.getActiveProfiles()).as("missing required profiles").contains("authenticated-access");
        BDDAssumptions.given(filterChain).as("no security filter chain found").isNotNull();
        BDDAssumptions.given(filterChain.getFilters().stream()
                                .filter(f-> f instanceof BasicAuthenticationFilter)
                                .findFirst().orElse(null))
                                .as("basic authentication not enabled in this configuration")
                                .isNotNull();
        try {
                rentersAPIClient.withRestTemplate(authnUser).removeAllRenters();
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            // ignored
            log.info("********************************************* Exception removing renters - {}" , ex.getMessage() );
        }

    }
    @Test
    void form_login_disabled(){
        BDDAssertions.then(filterChain.getFilters().stream()
                            .filter(f->f instanceof DefaultLoginPageGeneratingFilter)
                            .findFirst().orElse(null))
                            .as("form login is not disabled")
                            .isNull();
    }

    @Test
    void session_mgmt_disabled() {
        // given
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path("api/whoAmI").build().toUri();
        RequestEntity  request = RequestEntity.post(url).build();
        // when
        ResponseEntity<?> response = authnUser.exchange(request, String.class);
        // then
        BDDAssertions.then(response.getHeaders().get("Set-Cookie"))
                        .as("session management not disabled")
                        .isNull();
    }

    @Test
    void deny_bad_password() {
        // given  - assume
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path("api/whoAmI").build().toUri();
        RequestEntity request = RequestEntity.get(url).build();
        // when  - act
        HttpStatusCodeException ex = org.junit.jupiter.api.Assertions.assertThrows(HttpStatusCodeException.class,
                                                                                    ()-> badUser.exchange(request, String.class));
        // then - evaluate / assert
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        log.info("*********************   -  {}", ex.getResponseBodyAsString());
    }

    @Nested
    class granted_authenticated_access_to {
        private AutosAPI authnAutosClient;
        private RentersAPI authnRentersClient;
        private ApiTestHelper<RentalDTO> authnHelper;
        
        @BeforeEach
        void init(){
            authnAutosClient = autosAPIClient.withRestTemplate(authnUser);
            authnRentersClient = rentersAPIClient.withRestTemplate(authnUser);
            authnHelper = testHelper.withRestTemplate(authnUser);            
        }

        @Test
        void autos_post() {
            // given
            AutoDTO validAuto = autoDTOFactory.make();
            // when
            ResponseEntity<AutoDTO> response = authnAutosClient.createAuto(validAuto);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        void renters_post() {
            // given
            RenterDTO renterDTO = renterDTOFactory.make();
            // when
            ResponseEntity<RenterDTO> response = authnRentersClient.createRenter(renterDTO);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        //@Test
        void rentals_post(){
            // given
            AutoDTO auto = authnAutosClient.createAuto(autoDTOFactory.make()).getBody();
            RenterDTO renter = authnRentersClient.createRenter(renterDTOFactory.make()).getBody();
            TimePeriod timePeriod = new TimePeriod(LocalDate.now(), 5);
            AutoRentalDTO autoRentalDTO = (AutoRentalDTO) authnHelper.makeProposal(auto, renter, timePeriod);
            // when
            ResponseEntity<RentalDTO> response = authnHelper.createContract(autoRentalDTO);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        
    }

    String who_am_i(RestTemplate restTemplate, HttpMethod method) {
            // given
            URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path("api/whoAmI").build().toUri();
            RequestEntity request = RequestEntity.method(method, url).build();
            // when 
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            // then
            log.info("user ={}", body);
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            return body;
    }

    @Nested
    class identify_caller_for {
        @ParameterizedTest
        @ValueSource(strings ={"GET","POST"})
        void anonymous_user(String methodName) {
            // given
            HttpMethod method  = HttpMethod.valueOf(methodName);
            // when
            String identity = who_am_i(anonymousUser, method);
            // then
            BDDAssertions.then(identity).as("unexpected username").isEqualTo(anonymousUsername);
        }

        @ParameterizedTest
        @ValueSource(strings = {"GET", "POST"})
        void authenticated_user (String methodName) {
            // given
            HttpMethod method = HttpMethod.valueOf(methodName);
            // when
            String identity = who_am_i(authnUser, method);
            // then
            BDDAssertions.then(identity).as("unexpected username").isEqualTo(authnUsername);
        }
    }
        
    
}


