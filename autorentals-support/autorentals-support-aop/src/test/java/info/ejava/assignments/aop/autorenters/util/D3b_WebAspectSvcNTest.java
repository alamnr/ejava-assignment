package info.ejava.assignments.aop.autorenters.util;


import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.examples.common.exceptions.ClientErrorException;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

//@SpringBootTest(classes={AutoRentalsSecurityApp.class,
//        ProvidedAuthorizationTestHelperConfiguration.class},
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Slf4j
//@ActiveProfiles({"test", "authorizations", "authentication", "aop"})//aop profile!
//@DisplayName("Part D3b: Web Aspect (Service)")
@Slf4j
public class D3b_WebAspectSvcNTest {
    @Autowired
    private AutoDTOFactory autoFactory;
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired
    private AutosAPIClient autosService;
    @Autowired
    private RentersAPIClient rentersService;
    @Autowired
    private Environment env;

    @BeforeEach
    void init(@Autowired @Qualifier("authnUser") RestTemplate authnUser) {
        autosService = autosService.withRestTemplate(authnUser);
        rentersService = rentersService.withRestTemplate(authnUser);
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class can_proxy_createAuto {
        @BeforeAll
        void init() throws InvocationTargetException, IllegalAccessException {
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user0","password"));
        }

        Stream<Arguments> autos() {
            boolean aopActive = env.acceptsProfiles(Profiles.of("aop"));
            return Stream.of(
                    Arguments.of("valid", autoFactory.make(), null),
                    Arguments.of("id notNull", autoFactory.make(AutoDTOFactory.withId), "must be null"),
                    Arguments.of("username notNull", autoFactory.make().withUsername(autoFactory.username()),
                            aopActive ? "must be null" : null),
                    Arguments.of("make isNull", autoFactory.make().withMake(null),
                            aopActive ? "must not be null" : null),
                    Arguments.of("model isNull", autoFactory.make().withModel(null),
                            aopActive ? "must not be null" : null),
                    Arguments.of("passengers isNull", autoFactory.make().withPassengers(null),
                            aopActive ? "must not be null" : null),
                    Arguments.of("dailyRate isNull", autoFactory.make().withDailyRate(null),
                            aopActive ? "must not be null" : null),
                    Arguments.of("fuelType isNull", autoFactory.make().withFuelType(null),
                            aopActive ? "must not be null" : null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("autos")
        void given(String name, AutoDTO autoDTO, String errorMsg) {
            //when
            ClientErrorException.InvalidInputException ex = catchThrowableOfType(
                    () -> autosService.createAuto(autoDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                then(ex).isNull();
            } else {
                then(ex).as(()->"error not detected by dynamic proxy:" + autoDTO).isNotNull();
                log.info("{}", ex.getMessage());
                then(ex).hasMessageContaining(errorMsg);
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class can_proxy_updateAuto {
        @BeforeEach
        void init() throws InvocationTargetException, IllegalAccessException {
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user0","password"));
        }

        Stream<Arguments> autos() {
            boolean aopActive = env.acceptsProfiles(Profiles.of("aop"));
            return Stream.of(
                    Arguments.of("null id && username", autoFactory.make(), null),
                    Arguments.of("id notNull", autoFactory.make(AutoDTOFactory.withId), null),
                    Arguments.of("username notNull", autoFactory.make().withUsername(autoFactory.username()),
                            aopActive ? "must be null" : null),
                    Arguments.of("make isNull", autoFactory.make().withMake(null), null),
                    Arguments.of("model isNull", autoFactory.make().withModel(null), null),
                    Arguments.of("passengers isNull", autoFactory.make().withPassengers(null), null),
                    Arguments.of("dailyRate isNull", autoFactory.make().withDailyRate(null), null),
                    Arguments.of("fuelType isNull", autoFactory.make().withFuelType(null), null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("autos")
        void given(String name, AutoDTO autoDTO, String errorMsg) {
            AutoDTO auto = autosService.createAuto(autoFactory.make()).getBody();
            //when
            ClientErrorException.InvalidInputException ex = catchThrowableOfType(
                    () -> autosService.updateAuto(auto.getId(), autoDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                then(ex).isNull();
                AutoDTO resultingAuto = autosService.getAuto(auto.getId()).getBody();
                then(resultingAuto).isEqualTo(autoDTO.withId(auto.getId()));
            } else {
                then(ex).as(()->"error not detected by dynamic proxy:" + autoDTO).isNotNull();
                log.info("{}", ex.getMessage());
                then(ex).hasMessageContaining(errorMsg);
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class can_proxy_createRenters {
        int index=1;
        @BeforeEach
        void init() throws InvocationTargetException, IllegalAccessException {
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user" + index++,"password"));
        }

        Stream<Arguments> renters() {
            boolean aopActive = env.acceptsProfiles(Profiles.of("aop"));
            return Stream.of(
                    Arguments.of("valid", renterFactory.make(), null),
                    Arguments.of("id notNull", renterFactory.make(RenterDTOFactory.withId), "must be null"),
                    Arguments.of("username notNull", renterFactory.make().withUsername(renterFactory.username()),
                            aopActive ? "must be null" : null),
                    Arguments.of("email isNull", renterFactory.make().withEmail(null),
                            aopActive ? "must not be null" : null),
                    Arguments.of("firstName isNull", renterFactory.make().withFirstName(null),
                            aopActive ? "must not be null" : null),
                    Arguments.of("lastName isNull", renterFactory.make().withLastName(null),
                            aopActive ? "must not be null" : null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("renters")
        void given(String name, RenterDTO renterDTO, String errorMsg) {
            //when
            ClientErrorException.InvalidInputException ex = catchThrowableOfType(
                    () -> rentersService.createRenter(renterDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null == errorMsg) {
                then(ex).isNull();
            } else {
                then(ex).as(() -> "error not detected by dynamic proxy:" + renterDTO).isNotNull();
                log.info("{}", ex.getMessage());
                then(ex).hasMessageContaining(errorMsg);
            }
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class can_proxy_updateRenters {
        int index=1;
        @BeforeEach
        void init() throws InvocationTargetException, IllegalAccessException {
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user" + index++,"password"));
        }

        Stream<Arguments> renters() {
            boolean aopActive = env.acceptsProfiles(Profiles.of("aop"));
            return Stream.of(
                    Arguments.of("null id & username", renterFactory.make(), null),
                    Arguments.of("id notNull", renterFactory.make(RenterDTOFactory.withId), null),
                    Arguments.of("username notNull", renterFactory.make().withUsername(renterFactory.username()),
                            aopActive ? "must be null" : null),
                    Arguments.of("email isNull", renterFactory.make().withEmail(null), null),
                    Arguments.of("firstName isNull", renterFactory.make().withFirstName(null), null),
                    Arguments.of("lastName isNull", renterFactory.make().withLastName(null), null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("renters")
        void given(String name, RenterDTO renterDTO, String errorMsg) {
            RenterDTO renter = rentersService.createRenter(renterFactory.make()).getBody();
            //when
            ClientErrorException.InvalidInputException ex = catchThrowableOfType(
                    () -> rentersService.updateRenter(renter.getId(), renterDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                then(ex).isNull();
                RenterDTO resultingRenter = rentersService.getRenter(renter.getId()).getBody();
                then(resultingRenter).isEqualTo(renterDTO.withId(renter.getId()));
            } else {
                then(ex).as(()->"error not detected by dynamic proxy:" + renterDTO).isNotNull();
                log.info("{}", ex.getMessage());
                then(ex).hasMessageContaining(errorMsg);
            }
        }
    }
}
