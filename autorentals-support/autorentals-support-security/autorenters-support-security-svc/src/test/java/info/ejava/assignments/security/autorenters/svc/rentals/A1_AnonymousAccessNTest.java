package info.ejava.assignments.security.autorenters.svc.rentals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.List;


import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.SearchParams;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.svc.rentals.ApiTestHelper;
import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;

//@SpringBootTest(classes= { ...
//    },
//    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles({"test", "anonymous-access"})
//@DisplayName("Part A1: Anonymous Access")
@Slf4j
public class A1_AnonymousAccessNTest {
    
    @Autowired
    private ApiTestHelper<RentalDTO> testHelper;
    @Autowired
    private RestTemplate anonymousUser;
    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private AutosAPI autosClient;
    @Autowired
    private RentersAPI rentersClient;
    @Autowired
    private AutoDTOFactory autoDTOFactory;
    @Autowired
    private RenterDTOFactory renterDTOFactory;
    private @Autowired Environment env;
    @Autowired(required = false)
    private SecurityFilterChain filterChain;

    @BeforeEach
    void check() {
        Assumptions.assumeFalse(getClass().equals(A1_AnonymousAccessNTest.class),"should only run for deriverd class");
        BDDAssumptions.given(filterChain).as("no security filter chain found").isNotNull();
        BDDAssumptions.given(env.getActiveProfiles()).as("missing required profile").containsAnyOf("anonymous-access", "authenticated-access");
    }

    @Test
    void csrf_is_off() {
        BDDAssertions.then(filterChain.getFilters().stream()
                                .filter(f-> f instanceof CsrfFilter)
                                .findFirst().orElse(null))
                                .as("Csrf appears to be active")
                                .isNull();     
    }

    @Nested
    class granted_access_to {
        
        @Test 
        void static_content(){
            // given
            URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path("content/past_transaction.txt").build().toUri();
            RequestEntity<Void> request = RequestEntity.get(url)    .accept(MediaType.TEXT_PLAIN).build();
            try {
                ResponseEntity<String> response = anonymousUser.exchange(request, String.class);

                // then
                BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                String body = response.getBody();
                BDDAssertions.then(body).as("content was empty").isNotNull();
                BDDAssertions.then(body).as("got login page instead of static content")
                                .doesNotContain("Please sign in");
                BDDAssertions.then(body).as("did not get expected static content")
                                .contains("Past Autorentals");

            } catch(HttpClientErrorException.NotFound ex){
                BDDAssertions.fail("static content was not found at %s ", url);
            }
            
        }

        @Test
        void autos_safe_head_operation(){
            // when 
            HttpStatusCode status;
            try {
                status = autosClient.hasAuto("ANY_ID").getStatusCode();

            } catch (HttpStatusCodeException ex) {
                status = ex.getStatusCode();
            }

            // then
            BDDAssertions.then(status).as("denied anonymous access").isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void anyUrl_safe_head_operation(){
            // given
            URI anyUrl = UriComponentsBuilder.fromUriString("/anyUrl").build().toUri();
            RequestEntity<Void> request = RequestEntity.head(anyUrl).build();
            // when
            HttpStatusCode status;
            try {
                status = anonymousUser.exchange(request, Void.class).getStatusCode();
            } catch(HttpStatusCodeException ex ){
                status = ex.getStatusCode();
            }

            // then
            BDDAssertions.then(status).as("denied anonymous access").isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void autos_safe_get_parent_operation() {
            try {
                //given
                AutoDTO allAutos = AutoDTO.builder().build();
                //when
                ResponseEntity<AutoListDTO> response = autosClient.queryAutos(allAutos, 1, 0);
                //then
                BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                AutoListDTO autos = response.getBody();
            } catch(HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
                fail("denied anonymous access to root resource: " + ex);
            }
        }

        @Test
        void autos_safe_get_child_operation() {
            org.assertj.core.api.Assertions.assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .as("access not granted to child resource")
                    .isThrownBy(()->autosClient.getAuto("anId"));
        }

        @Test
        void renters_safe_head_operation() {
            //when
            HttpStatusCode status;
            try { //may get a 302 redirect to form login
                status = rentersClient.hasRenter("anId").getStatusCode();
            } catch (HttpStatusCodeException ex) {
                status = ex.getStatusCode();
            }
            //then
            BDDAssertions.then(status).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void rentals_safe_get_parent_operation() {
            try {
                //when
                List<RentalDTO> foundRentals = testHelper.findRentalsBy(SearchParams.builder()
                        .pageNumber(0).pageSize(1)
                        .build());
                //then
                log.info("rentals={}", foundRentals);
            } catch (HttpStatusCodeException ex) {
                fail("denied anonymous access to parent resource: " + ex);
            }
        }

        @Test
        void rentals_safe_get_child_operation() {
            org.assertj.core.api.Assertions.assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .as("denied anonymous access to child resource")
                    .isThrownBy(()->testHelper.getRentalById("anId"));
        }

    }

    @Nested
    class denied_for_access_to {

        @Test
        void autos_unsafe_post_operation(){
            // when
            HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                                            ()->autosClient.createAuto(autoDTOFactory.make()),
                                            "was not denied access");
            // then
            BDDAssertions.then(ex.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED,HttpStatus.FORBIDDEN);
        }

        @Test
        void autos_nonsafe_put_operation() {
            // given
            AutoDTO auto = autoDTOFactory.make(AutoDTOFactory.withId);
            // when
            HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class, 
                                            ()-> autosClient.updateAuto(auto.getId(), auto),
                                            "was not denied access");
            // then
            BDDAssertions.then(ex.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

         @Test
        void renters_unsafe_post_operation() {
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    ()-> rentersClient.createRenter(renterDTOFactory.make()),
                    "was not denied access");
            //then
            BDDAssertions.then(ex.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

        @Test
        void renters_unsafe_put_operation() {
            //given
            RenterDTO renter = renterDTOFactory.make(RenterDTOFactory.withId);
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    ()-> rentersClient.updateRenter(renter.getId(),renter),
                    "was not denied access");
            //then
            BDDAssertions.then(ex.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

        @Test
        void renters_safe_get_but_secured_get_operation() {
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    ()-> rentersClient.getRenters(null, null),
                    "was not denied access");
            //then
            BDDAssertions.then(ex.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

        @Test
        void renters_nonsafe_delete_operation() {
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    ()-> rentersClient.removeAllRenters());
            //then
            BDDAssertions.then(ex.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

        @Test
        void rentals_nonsafe_delete_operation() {
            //when
            try { //may get a 302 redirect to form login
                HttpStatusCode status = testHelper.removeAllRentals().getStatusCode();
                BDDAssertions.then(status).as("not denied access").isNotEqualTo(HttpStatus.NO_CONTENT);
                BDDAssertions.then(status.is2xxSuccessful()).as(()->"not denied access: "+status).isFalse();
            } catch (HttpStatusCodeException ex) {
                BDDAssertions.then(ex.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
            }
        }



    }




}
