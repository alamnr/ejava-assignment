package info.ejava.alamnr.assignment3.security.autorentals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ClassUtils;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.RequestMatcherDelegatingAuthorizationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import info.ejava.assignments.api.autorentals.svc.main.rental.ApiTestHelper;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.svc.autos.AutosController;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;



  /*
   *  N.B :  Notice also that our initial SecurityFilterChain is within the other chains in the
   *  example and is high in priority because of our @Order value assignment: 
   *  for this test in SecurityConfiguration$PartA_Authorizatonnn the Security Filter Chain Order is @Order(0) i.e. highest priority
   * 
   */

@SpringBootTest(classes= {
        AutoRentalsSecurityApp.class,
        SecurityTestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test","authorities", "authorization"})

@Slf4j
@DisplayName("Part B2: Authorization")
//@Disabled
public class MyB2_AuthorizationNTest_Extended {

    @Autowired
    private AutoDTOFactory autoFactory;
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired
    private AutosAPIClient autosClient;
    @Autowired @Qualifier("rentersAPIClient")
    private RentersAPIClient rentersClient;
    @Autowired
    private ApiTestHelper<RentalDTO> testHelper;
    @Autowired
    RestTemplate adminUser;
    @Autowired
    RestTemplate authnUser;
    @Autowired
    RestTemplate altUser;
    @Autowired
    RestTemplate proxyUser;
    @Autowired
    RestTemplate mgrUser;
    @Autowired Environment env;
    @Autowired ApplicationContext ctx;
    private List<AutoDTO> autos;

    @Autowired
    List<SecurityFilterChain> filterChain;

    @Autowired
    ApplicationContext context;

    @Autowired
    Map<String,RestTemplate>  authnUsers;
    
    @BeforeEach
    void init() {
        log.info("************************************************************ total filter chain - {}", filterChain.size());
        for (SecurityFilterChain  fChain : filterChain) {
            for (Filter filter : fChain.getFilters()) {
                if(filter instanceof AuthorizationFilter authFilter)
                {
                    log.info("**********************************************************  Authorization filter - {}", authFilter);
                     var manager = authFilter.getAuthorizationManager();

                    if (manager instanceof RequestMatcherDelegatingAuthorizationManager delegatingManager) {
                        // var mappings = delegatingManager.getMappings();
                        // mappings.forEach(mapping -> {
                        //     RequestMatcher matcher = mapping.getMatcher();
                        //     AuthorizationManager<?> authz = mapping.getAuthorizationManager();

                        //     System.out.printf("Matcher: %-30s -> Authorization: %s%n",
                        //             matcher, authz);});
                    }
                }
                
            }
        }

        // String[] beanNames = ctx.getBeanDefinitionNames();
        // Arrays.sort(beanNames);
        //Arrays.stream(beanNames).forEach(name->log.info("******** bean name - {} , class - {}",name, ctx.getBean(name).getClass().getName()));

        
        BDDAssumptions.given(env.getActiveProfiles()).as("missing required profile").contains("authorities", "authorization");
        String resource="autos";
        try {
            autosClient.withRestTemplate(adminUser).removeAllAutos();
            resource = "renters";
            rentersClient.withRestTemplate(adminUser).removeAllRenters();
            resource = "rentals";
            testHelper.withRestTemplate(adminUser).removeAllRentals();
        } catch (HttpClientErrorException.Forbidden ex) {
            fail(String.format("admin forbidden to delete %s, check authorities and SecurityFilterChain authorization", resource));
        } catch (HttpStatusCodeException ex) {
            fail(String.format("admin unable to delete %s", resource), ex);
        }
        
    } 

    /*
    @Test
    void context(){
        BDDAssertions.then(mgrUser).isNotNull();
        log.info("interceptor size - {}",adminUser.getInterceptors().size());
        // Print all interceptors
        for (ClientHttpRequestInterceptor interceptor : adminUser.getInterceptors()) {
            System.out.println("Interceptor: " + interceptor);

            if (interceptor instanceof BasicAuthenticationInterceptor basicAuth) {
                System.out.println("→ Basic Auth Username: " + basicAuth.getClass());
                // ⚠️ password is not directly exposed for security reasons
            }
        }

        authnUsers.forEach((k,v)-> log.info("key - {} , value - {} ",k,v));
    } */

    @Test
    void method_security_enabled() {
        Map<String,Object> configs = ctx.getBeansWithAnnotation(EnableMethodSecurity.class);

        BDDAssertions.then(configs).as(EnableMethodSecurity.class + " has not been enabled").isNotEmpty();
        BDDAssertions.then(configs).as(" unexpected number of classes with annotation: "+ EnableMethodSecurity.class).hasSize(1);
        Object config = configs.values().iterator().next();

        ClassUtils.getAllSuperclasses(config.getClass()).stream()
                    .map(clz->clz.getAnnotation(EnableMethodSecurity.class))
                    .filter(Objects::nonNull)
                    .forEach(annotation -> 
                                BDDAssertions.then(annotation.prePostEnabled()).as("expression method security not enabled").isTrue()
                    );

    }


    AutoDTO given_an_auto(RestTemplate user) {
        return autosClient.withRestTemplate(user).createAuto(autoFactory.make()).getBody();
    }
    RenterDTO given_a_renter(RestTemplate user) {
        return rentersClient.withRestTemplate(user).createRenter(renterFactory.make()).getBody();
    }

     @Nested
    class autos {
        private AutosAPIClient authnAutosClient;

        @Nested
        class authenticated_user {
            @BeforeEach
            void init() {
                authnAutosClient = autosClient.withRestTemplate(authnUser);
            }

            @Nested
            class may {
                @Test
                void create_auto() {
                    //given
                    AutoDTO validAuto = autoFactory.make();
                    //when
                    ResponseEntity<AutoDTO> response = authnAutosClient.createAuto(validAuto);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                }

                @Test
                void modify_their_auto() {
                    //given
                    AutoDTO existingAuto = given_an_auto(authnUser);
                    AutoDTO modifiedAuto = existingAuto
                            .withPassengers(existingAuto.getPassengers()+1)
                            .withId(null);
                    //when
                    ResponseEntity<AutoDTO> response = authnAutosClient.updateAuto(existingAuto.getId(), modifiedAuto);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    AutoDTO updatedAuto = response.getBody();
                    BDDAssertions.then(updatedAuto.getPassengers()).isEqualTo(modifiedAuto.getPassengers());
                }

                @Test
                void delete_their_auto() {
                    //given
                    AutoDTO existingAuto = given_an_auto(authnUser);
                    
                    //when
                    ResponseEntity<Void> response = authnAutosClient.removeAuto(existingAuto.getId());
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                    RestClientResponseException ex = assertThrows(RestClientResponseException.class,
                            () -> authnAutosClient.getAuto(existingAuto.getId()));
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    
                }

            }

            @Nested
            class may_not {
                private AutosAPIClient altAutosClient;

                @BeforeEach
                void init() {
                    altAutosClient = autosClient.withRestTemplate(altUser);
                }

                @Test
                void modify_anothers_auto() {
                    //given
                    AutoDTO existingAuto = given_an_auto(authnUser);
                    AutoDTO modifiedAuto = existingAuto
                            .withPassengers(existingAuto.getPassengers() + 1)
                            .withId(null);
                    //when
                    HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                            () -> altAutosClient.updateAuto(existingAuto.getId(), modifiedAuto));
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void delete_anothers_auto() {
                    //given
                    AutoDTO existingAuto = given_an_auto(authnUser);
                    //when
                    HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                            () -> altAutosClient.removeAuto(existingAuto.getId()),
                            "only owner should be able to delete their auto");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                 @Test
                void delete_all_autos() {
                    //given
                    AutoDTO existingAuto = given_an_auto(authnUser);
                    //when
                    HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                            () -> authnAutosClient.removeAllAutos(),
                            "only admins should be able to delete all autos");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    BDDAssertions.then(authnAutosClient.getAuto(existingAuto.getId()).getStatusCode()).isEqualTo(HttpStatus.OK);
                    BDDAssertions.then(authnAutosClient.queryAutos(AutoDTO.builder().build(), 0,1).getBody().getAutos()).isNotEmpty();
                }


            }

        }
    }


}
