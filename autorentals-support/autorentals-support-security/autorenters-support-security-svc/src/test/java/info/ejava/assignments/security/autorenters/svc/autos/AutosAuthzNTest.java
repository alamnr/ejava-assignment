package info.ejava.assignments.security.autorenters.svc.autos;

import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
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

import java.time.LocalDate;

import static info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory.withId;
import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes= {
        //AutoRenterTestConfiguration.class,
        AuthoritiesTestConfiguration.class,
        ProvidedAuthorizationTestHelperConfiguration.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableAutoConfiguration
@ActiveProfiles({"test","authorities"})
@Slf4j
public class AutosAuthzNTest {
    @Autowired
    private AutoDTOFactory autoFactory;
    @Autowired
    private AutosAPIClient autosAPIClient;

    @Nested
    class anonymous_user {
        private AutosAPI anonymousClient;

        @BeforeEach
        void init(@Autowired RestTemplate anonymousUser) {
            anonymousClient = autosAPIClient.withRestTemplate(anonymousUser);
        }

        @Nested
        class can {

            @Test
            void get_auto() {
                //verify
                assertThrows(HttpClientErrorException.NotFound.class,
                        () -> anonymousClient.hasAuto("aAutoId"));
            }

            @Test
            void check_auto_exists() {
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        () -> anonymousClient.hasAuto("aAutoId"),
                        HttpClientErrorException.class);
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }

            @Test
            void get_autos() {
                //given
                AutoDTO allAutos = AutoDTO.builder().build();
                //when
                ResponseEntity<AutoListDTO> response = anonymousClient.queryAutos(allAutos, 0, 1);
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            }
        }

        @Nested
        class cannot {
            @Test
            void add_auto() {
                //given
                AutoDTO auto = autoFactory.make();
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        ()->anonymousClient.createAuto(auto),
                        HttpClientErrorException.class
                );
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }

            @Test
            void update_auto() {
                //given
                AutoDTO auto = autoFactory.make(withId);
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        ()->anonymousClient.updateAuto(auto.getId(), auto),
                        HttpClientErrorException.class
                );
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }

            @Test
            void remove_auto() {
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        ()->anonymousClient.removeAuto("autoId"),
                        HttpClientErrorException.class
                );
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
        @Test
        void remove_all_autos() {
            //when
            HttpClientErrorException ex = catchThrowableOfType(
                    ()->anonymousClient.removeAllAutos(),
                    HttpClientErrorException.class
            );
            //then
            then(ex).as("no exception thrown").isNotNull();
            then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    class authenticated_user {
        private AutosAPI authnClient;
        private AutosAPI altClient;

        @BeforeEach
        void init(@Autowired RestTemplate authnUser, @Autowired RestTemplate altUser) {
            authnClient = autosAPIClient.withRestTemplate(authnUser);
            altClient = autosAPIClient.withRestTemplate(altUser);
        }

        @Nested
        class can {

            @Test
            void get_auto() {
                //verify
                assertThrows(HttpClientErrorException.NotFound.class,
                        () -> authnClient.hasAuto("aAutoId"));
            }

            @Test
            void check_auto_exists() {
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        () -> authnClient.hasAuto("aAutoId"),
                        HttpClientErrorException.class);
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }

            @Test
            void get_autos() {
                //given
                AutoDTO allAutos = AutoDTO.builder().build();
                //when
                ResponseEntity<AutoListDTO> response = authnClient.queryAutos(allAutos, 0, 1);
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            }

            @Test
            void add_auto() {
                //given
                AutoDTO auto = autoFactory.make();
                //when
                ResponseEntity<AutoDTO> response = authnClient.createAuto(auto);
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                then(response.getBody().getId()).isNotNull();
            }

            @Test
            void update_auto() {
                //given
                AutoDTO auto = authnClient.createAuto(autoFactory.make()).getBody();
                int modifiedPassengers = auto.getPassengers()-1;
                AutoDTO modifiedAuto = auto.withPassengers(modifiedPassengers);
                //when
                ResponseEntity<AutoDTO> response = authnClient.updateAuto(auto.getId(), modifiedAuto);
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                then(response.getBody().getPassengers()).isEqualTo(modifiedPassengers);
            }
            @Test
            void remove_their_auto() {
                //given
                AutoDTO auto = authnClient.createAuto(autoFactory.make()).getBody();
                //when
                ResponseEntity<Void> response = authnClient.removeAuto(auto.getId());
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        class cannot {
            @Test
            void update_another_users_auto() {
                //given
                AutoDTO auto = altClient.createAuto(autoFactory.make()).getBody();
                int modifiedPassengers = auto.getPassengers() - 1;
                AutoDTO modifiedAuto = auto.withPassengers(modifiedPassengers);
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        ()->authnClient.updateAuto(auto.getId(), modifiedAuto),
                        HttpClientErrorException.class
                );
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
            @Test
            void remove_another_users_auto() {
                //given
                AutoDTO auto = altClient.createAuto(autoFactory.make()).getBody();
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        ()->authnClient.removeAuto(auto.getId()),
                        HttpClientErrorException.class
                );
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
            @Test
            void remove_all_autos() {
                //when
                HttpClientErrorException ex = catchThrowableOfType(
                        ()->authnClient.removeAllAutos(),
                        HttpClientErrorException.class
                );
                //then
                then(ex).as("no exception thrown").isNotNull();
                then(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
        }
    }


    @Nested
    class mgr_user {
        private AutosAPI mgrClient;
        private AutosAPI altClient;

        @BeforeEach
        void init(@Autowired RestTemplate mgrUser, @Autowired RestTemplate altUser) {
            mgrClient = autosAPIClient.withRestTemplate(mgrUser);
            altClient = autosAPIClient.withRestTemplate(altUser);
        }
        @Nested
        class can {
            @Test
            void remove_another_users_auto() {
                //given
                AutoDTO auto = altClient.createAuto(autoFactory.make()).getBody();
                //when
                ResponseEntity<Void> response = mgrClient.removeAuto(auto.getId());
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        class cannot {
            @Test
            void remove_all_autos() {
                //verify
                assertThrows(HttpClientErrorException.Forbidden.class,
                        ()->mgrClient.removeAllAutos());
            }
        }
    }

    @Nested
    class admin_user {
        private AutosAPI adminClient;
        private AutosAPI altClient;

        @BeforeEach
        void init(@Autowired RestTemplate adminUser, @Autowired RestTemplate altUser) {
            adminClient = autosAPIClient.withRestTemplate(adminUser);
            altClient = autosAPIClient.withRestTemplate(altUser);
        }
        @Nested
        class can {
            @Test
            void remove_all_autos() {
                //when
                ResponseEntity<Void> response = adminClient.removeAllAutos();
                //then
                then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        class cannot {
        }
    }

}
