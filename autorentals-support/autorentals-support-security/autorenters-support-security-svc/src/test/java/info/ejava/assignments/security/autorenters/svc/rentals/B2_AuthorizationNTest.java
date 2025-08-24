package info.ejava.assignments.security.autorenters.svc.rentals;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ClassUtils;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import info.ejava.assignments.api.autorentals.svc.main.rental.ApiTestHelper;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;

import lombok.extern.slf4j.Slf4j;

//@SpringBootTest(classes= { ...
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles({"test","authorities", "authorization"})
//@DisplayName("Part B2: Authorization")
@Slf4j
public class B2_AuthorizationNTest {
    
    @Autowired AutoDTOFactory autoDTOFactory;
    @Autowired RenterDTOFactory renterDTOFactory;

    @Autowired AutosAPIClient autosClient;
    @Autowired RentersAPIClient rentersClient;

    @Autowired ApiTestHelper<RentalDTO> testHelper;

    @Autowired RestTemplate adminUser;
    @Autowired RestTemplate authnUser;
    @Autowired RestTemplate altUser;
    @Autowired RestTemplate proxyUser;
    @Autowired RestTemplate mgrUser;
    @Autowired Environment env;
    @Autowired ApplicationContext ctx;

    private List<AutoDTO> autos;


    @BeforeEach
    void init() {
        BDDAssumptions.given(env.getActiveProfiles()).as("missing required profile").contains("authorities","authorization");
        String resource = "autos";
        try {
            autosClient.withRestTemplate(adminUser).removeAllAutos();
            resource ="renters";
            rentersClient.withRestTemplate(adminUser).removeAllRenters();
            resource = "rentals";
            testHelper.withRestTemplate(adminUser).removeAllRentals();
        } catch (HttpClientErrorException.Forbidden ex) {
            fail(String.format("admin forbidden to delete %s, check authorities and SecurityFilterChain authorization", resource));
        } catch (HttpStatusCodeException ex) {
            fail(String.format("admin unable to delete %s", resource), ex);
        }

    }

    @Test
    void method_security_enabled() {
        Map<String,Object> configs = ctx.getBeansWithAnnotation(EnableMethodSecurity.class);
        BDDAssertions.then(configs).as(()->EnableMethodSecurity.class + "has not been enabled").isNotEmpty();
        BDDAssertions.then(configs).as("unexpected number of classes with annotation: "+ EnableMethodSecurity.class).hasSize(1);
        Object config = configs.values().iterator().next();

        ClassUtils.getAllSuperclasses(config.getClass()).stream()
                    .map(clz->clz.getAnnotation(EnableMethodSecurity.class))
                    .filter(Objects::nonNull)
                    .forEach(annotation->BDDAssertions.then(annotation.prePostEnabled()).as("expression method security not enabled")
                    .isTrue());
    }

    AutoDTO given_an_auto(RestTemplate user) {
        return autosClient.withRestTemplate(user).createAuto(autoDTOFactory.make()).getBody();
    }

    RenterDTO given_a_renter(RestTemplate user) {
        return rentersClient.withRestTemplate(user).createRenter(renterDTOFactory.make()).getBody();
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
                    AutoDTO validAuto = autoDTOFactory.make();
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
                    RestClientResponseException ex = Assertions.assertThrows(RestClientResponseException.class,
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
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> altAutosClient.updateAuto(existingAuto.getId(), modifiedAuto));
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }
                @Test
                void delete_anothers_auto() {
                    //given
                    AutoDTO existingAuto = given_an_auto(authnUser);
                    //when
                    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class,
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
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> authnAutosClient.removeAllAutos(),
                            "only admins should be able to delete all autos");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    BDDAssertions.then(authnAutosClient.getAuto(existingAuto.getId()).getStatusCode()).isEqualTo(HttpStatus.OK);
                    BDDAssertions.then(authnAutosClient.queryAutos(AutoDTO.builder().build(), 0,1).getBody().getAutos()).isNotEmpty();
                }
            }
        }

        @Nested
        class unauthenticated_user {
            @Test
            void may_not_create_auto() {
                //given
                AutoDTO validAuto = autoDTOFactory.make();
                //when
                RestClientResponseException ex = Assertions.assertThrows(RestClientResponseException.class,
                        () -> autosClient.createAuto(validAuto));
                //then
                BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Nested
        class admin_user {
            private AutosAPI adminAutosClient;

            @BeforeEach
            void init() {
                adminAutosClient = autosClient.withRestTemplate(adminUser);
            }
            @Test
            void can_delete_all_autos() {
                //given
                AutoDTO existingAuto = given_an_auto(altUser);
                //when
                ResponseEntity<Void> response = adminAutosClient.removeAllAutos();
                //then
                BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            }
        }
    }


    @Nested
    class renters {
        @Nested
        class authenticated_user {
            private RentersAPI authnRentersClient;

            @BeforeEach
            void init() {
                authnRentersClient = rentersClient.withRestTemplate(authnUser);
            }

            @Nested
            class may {
                @Test
                void create_their_renter() {
                    //given
                    RenterDTO validRenter = renterDTOFactory.make();
                    //when
                    ResponseEntity<RenterDTO> response = authnRentersClient.createRenter(validRenter);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                }

                @Test
                void modify_their_renter() {
                    //given
                    RenterDTO existingRenter = given_a_renter(authnUser);
                    RenterDTO modifiedRenter = existingRenter.withFirstName(existingRenter.getFirstName() + " modified");
                    //when
                    ResponseEntity<RenterDTO> response = authnRentersClient.updateRenter(existingRenter.getId(), modifiedRenter);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    RenterDTO updatedRenter = response.getBody();
                    BDDAssertions.then(updatedRenter.getFirstName()).isEqualTo(modifiedRenter.getFirstName()).contains("modified");
                }

                @Test
                void delete_their_renter() {
                    //given
                    RenterDTO existingRenter = given_a_renter(authnUser);
                    //when
                    ResponseEntity<Void> response = authnRentersClient.removeRenter(existingRenter.getId());
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                    BDDAssertions.thenExceptionOfType(HttpClientErrorException.NotFound.class)
                            .isThrownBy(() -> authnRentersClient.getRenter(existingRenter.getId()))
                            .withMessageContaining("not found");
                }
            }

            @Nested
            @DirtiesContext
            class may_not {
                RenterDTO anotherRenter;
                RentersAPI adminRentersClient;

                @BeforeEach
                void init() {
                    adminRentersClient = rentersClient.withRestTemplate(adminUser);
//                    adminRentersClient.removeAllRenters();
                    anotherRenter = rentersClient.withRestTemplate(altUser).createRenter(renterDTOFactory.make()).getBody();
                }

                @Test
                void modify_anothers_renter() {
                    //given
                    RenterDTO modifiedRenter = anotherRenter.withFirstName(anotherRenter.getFirstName() + " modified");
                    //when
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> authnRentersClient.updateRenter(anotherRenter.getId(), modifiedRenter));
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void delete_anothers_renter() {
                    //when
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> authnRentersClient.removeRenter(anotherRenter.getId()),
                    "only owner should be able to delete their renter");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void delete_all_renters() {
                    //when
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> authnRentersClient.removeAllRenters(),
                    "only admins should be able to delete all renters");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    BDDAssertions.then(adminRentersClient.getRenters(0, 1).getBody().getRenters()).isNotEmpty();
                }
            }
        }

        @Nested
        class unauthenticated_user {
            @Nested
            class may_not {
                @Test
                void create_renter() {
                    //given
                    RenterDTO validRenter = renterDTOFactory.make();
                    //when
                    RestClientResponseException ex = Assertions.assertThrows(RestClientResponseException.class,
                            () -> rentersClient.createRenter(validRenter));
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                }
            }
        }

        @Nested
        class admin_user {
            private RentersAPI adminRenterClient;

            @BeforeEach
            void init() {
                adminRenterClient = rentersClient.withRestTemplate(adminUser);
            }

            @Nested
            class can {
                @Test
                void delete_all_renters() {
                    //given
                    given_a_renter(altUser);
                    BDDAssertions.then(adminRenterClient.getRenters(0, 1).getBody().getRenters()).isNotEmpty();
                    //when
                    ResponseEntity<Void> response = adminRenterClient.removeAllRenters();
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                    BDDAssertions.then(adminRenterClient.getRenters(0, 1).getBody().getRenters()).isEmpty();
                }
            }
        }
    }

    @Nested
    class rentals {
        private TimePeriod nextTimePeriod = new TimePeriod(LocalDate.now(), 7);

        protected TimePeriod given_a_time_period() {
            TimePeriod current = nextTimePeriod;
            nextTimePeriod = nextTimePeriod.next();
            return current;
        }

        AutoDTO given_an_auto() {
            return autosClient.withRestTemplate(adminUser).createAuto(autoDTOFactory.make()).getBody();
        }
        RenterDTO given_a_renter(RestTemplate user) {
            return rentersClient.withRestTemplate(user).createRenter(renterDTOFactory.make()).getBody();
        }
        RentalDTO given_a_proposal(RenterDTO renter) {
            Objects.requireNonNull(renter);
            TimePeriod timePeriod = given_a_time_period();
            AutoDTO auto = given_an_auto();
            return testHelper.makeProposal(auto, renter, timePeriod);
        }
        RentalDTO given_a_proposal(RestTemplate user) {
            AutoDTO auto = given_an_auto();
            RenterDTO renter = given_a_renter(user);
            TimePeriod timePeriod = given_a_time_period();
            return testHelper.makeProposal(auto, renter, timePeriod);
        }
        RentalDTO given_a_contract(RestTemplate user) {
            RentalDTO proposal = given_a_proposal(user);
            return testHelper.withRestTemplate(user).createContract(proposal).getBody();
        }


        @Nested
        class authenticated_users {
            AutosAPI authnAutosClient;
            RentersAPI authnRentersClient;
            ApiTestHelper<RentalDTO> authnHelper;

            @BeforeEach
            void init() {
                authnAutosClient = autosClient.withRestTemplate(authnUser);
                authnRentersClient = rentersClient.withRestTemplate(authnUser);
                authnHelper = testHelper.withRestTemplate(authnUser);
            }

            @Nested
            class may {
                @Test
                void create_contract_for_existing_auto() {
                    //given
                    RentalDTO proposal = given_a_proposal(authnUser);
                    //when
                    ResponseEntity<RentalDTO> response = authnHelper.createContract(proposal);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                }

                @Test
                void delete_their_own_contract() {
                    //given
                    RentalDTO contract = given_a_contract(authnUser);
                    String rentalId = testHelper.getRentalId(contract);
                    //when
                    ResponseEntity<Void> response = authnHelper.removeRental(rentalId);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                }
            }

            @Nested
            class may_not {
                AutosAPI altAutoClient;
                ApiTestHelper<RentalDTO> altHelper;

                @BeforeEach
                void init() {
                    altAutoClient = autosClient.withRestTemplate(altUser);
                    altHelper = testHelper.withRestTemplate(altUser);
                }

                @Test
                void create_contract_for_other_user() {
                    //given
                    RentalDTO proposal = given_a_proposal(altUser);
                    //when
                    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class,
                            () -> authnHelper.createContract(proposal),
                            "only PROXY role should be able to create rental for other user");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void modify_contract_for_other_user() {
                    //given
                    RentalDTO rental = given_a_contract(altUser);
                    TimePeriod newTimePeriod = given_a_time_period();
                    testHelper.setStartDate(rental, newTimePeriod.getStartDate());
                    testHelper.setEndDateDate(rental, newTimePeriod.getEndDate());
                    //when
                    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class,
                            () -> authnHelper.modifyContract(rental),
                            "only PROXY role should be able to modify rental for other user");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void delete_anothers_contract() {
                    //given
                    RentalDTO contract = given_a_contract(altUser);
                    String rentalId = testHelper.getRentalId(contract);
                    //when
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> authnHelper.removeRental(rentalId),
                            "only owner should be able to delete their rental");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void cannot_delete_all_contracts() {
                    //given
                    RentalDTO rental = given_a_contract(authnUser);
                    String rentalId = testHelper.getRentalId(rental);
                    //when
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> authnHelper.removeAllRentals(),
                            "only admins should be able to delete all rentals");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    BDDAssertions.then(authnHelper.getRentalById(rentalId).getStatusCode()).isEqualTo(HttpStatus.OK);
                }
            }
        }

        @Nested
        class proxy_user {
            ApiTestHelper<RentalDTO> proxyHelper;

            @BeforeEach
            void init() {
                proxyHelper = testHelper.withRestTemplate(proxyUser);
            }

            @Nested
            class may {
                @Test
                void create_contract_for_other_user() {
                    //given
                    RenterDTO renter = given_a_renter(authnUser);
                    RentalDTO proposal = given_a_proposal(renter);
                    //when
                    ResponseEntity<RentalDTO> response = proxyHelper.createContract(proposal);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    String renterId = testHelper.getRenterId(response.getBody());
                    BDDAssertions.then(renterId).as("proxy should not be owner").isEqualTo(renter.getId());
                }

                @Test
                void modify_contract_for_other_user() {
                    //given
                    RentalDTO rental = given_a_contract(authnUser);
                    String ownerId = testHelper.getRenterId(rental);
                    TimePeriod newTimePeriod = given_a_time_period();
                    testHelper.setStartDate(rental, newTimePeriod.getStartDate());
                    testHelper.setEndDateDate(rental, newTimePeriod.getEndDate());
                    //when
                    ResponseEntity<RentalDTO> response = proxyHelper.modifyContract(rental);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    String renterId = testHelper.getRenterId(response.getBody());
                    BDDAssertions.then(renterId).as("proxy should not be owner").isEqualTo(ownerId);
                }
            }

            @Nested
            class may_not {

            }
        }

        @Nested
        class mgr {
            ApiTestHelper<RentalDTO> mgrHelper;

            @BeforeEach
            void init() {
                mgrHelper = testHelper.withRestTemplate(mgrUser);
            }

            @Nested
            class may {
                @Test
                void delete_contract() {
                    //given
                    RentalDTO proposal = given_a_contract(authnUser);
                    String rentalId = testHelper.getRentalId(proposal);
                    //when
                    ResponseEntity<Void> response = mgrHelper.removeRental(rentalId);
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> testHelper.withRestTemplate(authnUser).getRentalById(rentalId));
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo((HttpStatus.NOT_FOUND));
                }
            }

            @Nested
            class may_not {
                @Test
                void create_contract_for_other_user() {
                    //given
                    RenterDTO renter = given_a_renter(authnUser);
                    RentalDTO proposal = given_a_proposal(renter);
                    //when
                    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class,
                            () -> mgrHelper.createContract(proposal),
                            "only PROXY role should be able to create rental for other user");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void modify_contract_for_other_user() {
                    //given
                    RentalDTO rental = given_a_contract(altUser);
                    TimePeriod originalPeriod = testHelper.getTimePeriod(rental);
                    TimePeriod newTimePeriod = given_a_time_period();
                    testHelper.setStartDate(rental, newTimePeriod.getStartDate());
                    testHelper.setEndDateDate(rental, newTimePeriod.getEndDate());
                    //when
                    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class,
                            () -> mgrHelper.modifyContract(rental),
                            "only PROXY role should be able to modify rental for other user");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    RentalDTO current = mgrHelper.getRental(rental).getBody();
                    BDDAssertions.then(testHelper.getTimePeriod(current))
                            .as("forbidden change should not have changed state")
                            .isEqualTo(originalPeriod);
                }
            }
        }
        @Nested
        class admin {
            ApiTestHelper<RentalDTO> adminHelper;

            @BeforeEach
            void init() {
                adminHelper = testHelper.withRestTemplate(adminUser);
            }

            @Nested
            class may {
                @Test
                void delete_all_rentals() {
                    //given
                    RentalDTO contract = given_a_contract(authnUser);
                    String rentalId = testHelper.getRentalId(contract);
                    //when
                    ResponseEntity<Void> response = adminHelper.removeAllRentals();
                    //then
                    BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> adminHelper.getRentalById(rentalId));
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo((HttpStatus.NOT_FOUND));
                }
                @Test
                void inherit_delete_contract_authority_from_mgr() {
                    //given
                    RentalDTO contract = given_a_contract(authnUser);
                    String rentalId = testHelper.getRentalId(contract);
                    //when
                    Assertions.assertDoesNotThrow(()->{
                        ResponseEntity<Void> response = adminHelper.removeRental(rentalId);
                        //then
                        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                    },"removeRental as ADMIN failed, check role inheritance");

                    //then
                    HttpStatusCodeException ex = Assertions.assertThrows(HttpStatusCodeException.class,
                            () -> testHelper.withRestTemplate(authnUser).getRentalById(rentalId));
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo((HttpStatus.NOT_FOUND));
                }
            }

            @Nested
            class may_not {
                @Test
                void create_contract_for_another_user() {
                    //given
                    RenterDTO renter = given_a_renter(authnUser);
                    RentalDTO proposal = given_a_proposal(renter);
                    //when
                    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class,
                            () -> adminHelper.createContract(proposal));
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                void modify_contract_for_other_user() {
                    //given
                    RentalDTO rental = given_a_contract(altUser);
                    TimePeriod originalPeriod = testHelper.getTimePeriod(rental);
                    TimePeriod newTimePeriod = given_a_time_period();
                    testHelper.setStartDate(rental, newTimePeriod.getStartDate());
                    testHelper.setEndDateDate(rental, newTimePeriod.getEndDate());
                    //when
                    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class,
                            () -> adminHelper.modifyContract(rental),
                            "only PROXY role should be able to modify rental for other user");
                    //then
                    BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    RentalDTO current = adminHelper.getRental(rental).getBody();
                    BDDAssertions.then(testHelper.getTimePeriod(current))
                            .as("forbidden change should not have changed state")
                            .isEqualTo(originalPeriod);
                }
            }
        }
    }
}

