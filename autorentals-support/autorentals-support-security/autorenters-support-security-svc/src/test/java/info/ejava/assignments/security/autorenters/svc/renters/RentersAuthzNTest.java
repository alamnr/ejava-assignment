package info.ejava.assignments.security.autorenters.svc.renters;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.assignments.security.autorenters.svc.testapp.AuthoritiesTestConfiguration;
import info.ejava.assignments.security.autorenters.svc.ProvidedAuthorizationTestHelperConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory.withId;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes={
        ProvidedAuthorizationTestHelperConfiguration.class,
        AuthoritiesTestConfiguration.class,
        //AutoRenterTestConfiguration.class
    }, properties = {"debug=true"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ActiveProfiles({"test","authorities"})
@Slf4j
public class RentersAuthzNTest {
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired
    private RentersAPIClient rentersClient;

    @BeforeEach
    void cleanup(@Autowired RestTemplate adminUser) {
        rentersClient.withRestTemplate(adminUser).removeAllRenters();
    }

    @Nested
    class anonymous_user {
        private RentersAPI anonymousClient;
        @BeforeEach
        void init(@Autowired RestTemplate anonymousUser) {
            anonymousClient = rentersClient.withRestTemplate(anonymousUser);
        }

        @Nested
        class can {
            @Test
            void check_renter_exists() {
                //verify
                assertThrows(HttpClientErrorException.NotFound.class,
                        ()->anonymousClient.hasRenter("renterId"));
            }
        }

        @Nested
        class cannot{
            @Test
            void get_renter() {
                //verify
                assertThrows(HttpClientErrorException.Unauthorized.class,
                        ()->anonymousClient.getRenter("renterId"));
            }

            @Test
            void get_renters() {
                //verify
                assertThrows(HttpClientErrorException.Unauthorized.class,
                        ()->anonymousClient.getRenters(null, null));
            }

            @Test
            void add_renter() {
                //given
                RenterDTO renter = renterFactory.make();
                //verify
                assertThrows(HttpClientErrorException.Unauthorized.class,
                        ()->anonymousClient.createRenter(renter));
            }

            @Test
            void update_renter() {
                //given
                RenterDTO renter = renterFactory.make(withId);
                //verify
                assertThrows(HttpClientErrorException.Unauthorized.class,
                        ()->anonymousClient.updateRenter(renter.getId(),renter));
            }

            @Test
            void remove_renter() {
                //verify
                assertThrows(HttpClientErrorException.Unauthorized.class,
                        ()->anonymousClient.removeRenter("renterId"));
            }

            @Test
            void remove_all_renters() {
                //verify
                assertThrows(HttpClientErrorException.Unauthorized.class,
                        ()->anonymousClient.removeAllRenters());
            }
        }
    }
    @Nested
    class authenticated_user {
        private RentersAPI authnClient;
        private RentersAPI altClient;
        @BeforeEach
        void init(@Autowired RestTemplate authnUser, @Autowired RestTemplate altUser) {
            authnClient = rentersClient.withRestTemplate(authnUser);
            altClient = rentersClient.withRestTemplate(altUser);
        }

        @Nested
        class can {
            @Test
            void check_renter_exists() {
                //verify
                assertThrows(HttpClientErrorException.NotFound.class,
                        ()->authnClient.hasRenter("renterId"));
            }

            @Test
            void add_renter() {
                //given
                RenterDTO renter = renterFactory.make();
                //when
                ResponseEntity<RenterDTO> response = authnClient.createRenter(renter);
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                then(response.getBody().getId()).isNotNull();
            }

            @Test
            void update_their_renter() {
                //given
                RenterDTO renter = authnClient.createRenter(renterFactory.make()).getBody();
                String updatedEmail = renter.getEmail() + "X";
                RenterDTO updatedRenter = renter.withEmail(updatedEmail);
                //when
                ResponseEntity<RenterDTO> response = authnClient.updateRenter(renter.getId(), updatedRenter);
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                then(response.getBody()).isEqualTo(updatedRenter);
            }
            @Test
            void get_their_renter() {
                //given
                RenterDTO renter = authnClient.createRenter(renterFactory.make()).getBody();
                //when
                ResponseEntity<RenterDTO> response = authnClient.getRenter(renter.getId());
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                then(response.getBody()).isEqualTo(renter);
            }
        }

        @Nested
        class cannot{
            @Test
            void get_nonexistant_renter() {
                //verify
                assertThrows(HttpClientErrorException.NotFound.class,
                        ()->authnClient.getRenter("aRenter"));
            }
            @Test
            void get_other_existing_renter() {
                RenterDTO renter = altClient.createRenter(renterFactory.make()).getBody();
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->authnClient.getRenter(renter.getId()));
            }

            @Test
            void get_renters() {
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->authnClient.getRenters(null, null));
            }

            @Test
            void remove_another_renter() {
                RenterDTO altRenter = altClient.createRenter(renterFactory.make()).getBody();
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->authnClient.removeRenter(altRenter.getId()));
            }
            @Test
            void remove_all_renters() {
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->authnClient.removeAllRenters());
            }
            @Test
            void update_another_renter() {
                //given
                RenterDTO renter = altClient.createRenter(renterFactory.make()).getBody();
                String updatedEmail = renter.getEmail() + "X";
                RenterDTO updatedRenter = renter.withEmail(updatedEmail);
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->authnClient.updateRenter(renter.getId(), updatedRenter));
            }
        }
    }

    @Nested
    class proxy_user {
        private RentersAPI proxyClient;
        private RentersAPI altClient;
        @BeforeEach
        void init(@Autowired RestTemplate proxyUser, @Autowired RestTemplate altUser) {
            proxyClient = rentersClient.withRestTemplate(proxyUser);
            altClient = rentersClient.withRestTemplate(altUser);
        }

        @Nested
        class can {
            @Test
            void can_get_another_renter() {
                RenterDTO renter = altClient.createRenter(renterFactory.make()).getBody();
                //verify
                ResponseEntity<RenterDTO> response = proxyClient.getRenter(renter.getId());
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                then(response.getBody().getId()).isEqualTo(renter.getId());
            }
        }
    }

    @Nested
    class mgr_user {
        private RentersAPI mgrClient;
        private RentersAPI altClient;
        @BeforeEach
        void init(@Autowired RestTemplate mgrUser, @Autowired RestTemplate altUser) {
            mgrClient = rentersClient.withRestTemplate(mgrUser);
            altClient = rentersClient.withRestTemplate(altUser);
        }

        @Nested
        class can {
            @Test
            void get_any_renter() {
                //given
                RenterDTO renter = altClient.createRenter(renterFactory.make()).getBody();
                //when
                ResponseEntity<RenterDTO> response = mgrClient.getRenter(renter.getId());
                //given
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                then(response.getBody()).isEqualTo(renter);
            }
            @Test
            void get_all_renters() {
                //when
                ResponseEntity<RenterListDTO> response = mgrClient.getRenters(null, null);
                //given
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            }
            @Test
            void remove_renter() {
                //given
                RenterDTO renter = altClient.createRenter(renterFactory.make()).getBody();
                //when
                ResponseEntity<Void> response = mgrClient.removeRenter(renter.getId());
                //given
                then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        class cannot {
            @Test
            void remove_all_renters() {
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->mgrClient.removeAllRenters());
            }
        }
    }

    @Nested
    class admin_user {
        private RentersAPI authnClient;
        private RentersAPI adminClient;
        @BeforeEach
        void init(@Autowired RestTemplate adminUser, @Autowired RestTemplate authnUser) {
            authnClient = rentersClient.withRestTemplate(authnUser);
            adminClient=rentersClient.withRestTemplate(adminUser);
        }

        @Nested class can {
            @Test
            void delete_all_renters() {
                //when
                ResponseEntity<Void> response = adminClient.removeAllRenters();
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            }

        }

        @Nested class cannot {
            @Test
            void get_nonexistant_renter() {
                //verify
                assertThrows(HttpClientErrorException.NotFound.class,
                        ()->adminClient.getRenter("aRenter"));
            }
            @Test
            void get_existing_renter() {
                final RenterDTO renter = authnClient.createRenter(renterFactory.make()).getBody();
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->adminClient.getRenter(renter.getId()));
            }
            @Test
            void get_all_renters() {
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->adminClient.getRenters(null, null));
            }
            @Test
            void update_any_renter() {
                //given
                final RenterDTO renter = authnClient.createRenter(renterFactory.make()).getBody();

                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->adminClient.updateRenter(renter.getId(), renter));
            }
        }
    }
}
