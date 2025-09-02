package info.ejava.alamnr.assignment3.security.autorentals;

import java.net.URI;

import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import info.ejava.assignments.api.autorentals.svc.main.rental.ApiTestHelper;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.assignments.security.autorenters.svc.rentals.A2b_NoSecurityNTest;
import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {
    AutoRentalsSecurityApp.class,
    SecurityTestConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test","nosecurity"})
@Slf4j
@DisplayName("Part A2b: No Security")
// @Disabled
public class MyA2b_NoSecurityNTest_Extended {
    
    @Autowired
    private ApiTestHelper<RentalDTO> testHelper;
    @Autowired
    private RestTemplate anonymousUser;
    @Autowired
    private ServerConfig serverConfig;
    @Autowired  @Qualifier("autosAPIClient")
    private AutosAPI autosClient;
    @Autowired @Qualifier("rentersAPIClient")
    private RentersAPI rentersClient;
    @Autowired
    private String anonymousUsername;
    private @Autowired Environment env;
    @Autowired(required = false) 
    private SecurityFilterChain filterChain;

    @BeforeEach
    void check(){
        Assumptions.assumeFalse(getClass().equals(A2b_NoSecurityNTest.class), "should only run on derived class");
        BDDAssumptions.given(filterChain).as("no security filter chain found").isNotNull();
        BDDAssumptions.given(env.getActiveProfiles()).as("missing required profile").contains("nosecurity");
    }

    @Test
    void csrf_is_off() {
        BDDAssertions.then(filterChain.getFilters().stream()
                            .filter(f->f instanceof CsrfFilter)
                            .findFirst().orElse(null))
                            .as("csrf is not disabled ").isNull();        
    }

    @Nested
    class granted_access_to {
        
        @Test
        void static_content() {
            // given
            URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path("content/past_transactions.txt").build().toUri();
            RequestEntity request = RequestEntity.get(url).accept(MediaType.TEXT_PLAIN).build();
            // when
            ResponseEntity<String> response = anonymousUser.exchange(request, String.class);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            String body = response.getBody();
            log.info("static content = {}", body);
            BDDAssertions.then(body).isNotNull();
            BDDAssertions.then(body).contains("Past Autorentals");
        }

        @Test
        void autos_safe_operation(){
            // given
            AutoSearchParams allAutos = AutoSearchParams.builder().build().page(0, 1);
            // when
            ResponseEntity<AutoListDTO> response = autosClient.searchAutos(allAutos);
            // then 
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            AutoListDTO autos = response.getBody();
            log.info("************************************ autos = {}", autos);
        }

        @Test
        void renters_safe_operation() {
            // when
            ResponseEntity<RenterListDTO> response = rentersClient.getRenters(null, 1);
            // then
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            RenterListDTO renters = response.getBody();
            log.info("*************************************** renters - {}", renters);
        }

        @Test
        void rentals_safe_operation() {
            // when
            Assertions.assertDoesNotThrow(()->{
                testHelper.findRentals(RentalSearchParams.builder().pageSize(1).build());

            }, " unexpected exception for finder");
        }

        @Test
        void autos_nonsafe_operation() {
            // when
            Assertions.assertDoesNotThrow(()-> autosClient.removeAllAutos(),"unexpected exception for remove autos");
        }

        @Test
        void renters_nonsafe_operation() {
            // when
            Assertions.assertDoesNotThrow(()->rentersClient.removeAllRenters(),"unexpected exception for remove renters");
        }

        @Test
        void rentals_nonosafe_operation() {
            // when
            Assertions.assertDoesNotThrow(()-> testHelper.removeAllRentals(),"unexpected exception for remove rentals");
        }

        @ParameterizedTest
        @ValueSource(strings = {"GET","POST"})
        void who_am_i(String methodName){

            // given
            HttpMethod method = HttpMethod.valueOf(methodName);
            URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path("/api/whoAmI").build().toUri();
            RequestEntity request = RequestEntity.method(method, url).build();
            // when
            ResponseEntity<String> response = anonymousUser.exchange(request, String.class);
            String body = response.getBody();
            // then
            log.info("************************* user - {}", body);
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            BDDAssertions.then(body).isEqualTo(anonymousUsername);

        }

    }

}
