package info.ejava.alamnr.assignment3.aop;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes={AutoRentalsSecurityApp.class, AutoTestConfiguration.class})
@Slf4j
@ActiveProfiles({"test", "nosecurity"})//no aop profile!
@DisplayName("Part D3a1: No Aspect (Service)")
public class MyD3a1_NoAspectSvcNTest_Extended {
    @Autowired
    private AutoDTOFactory autoFactory;
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired
    private AutosService autosService;
    @Autowired
    private RentersService rentersService;
    @Autowired
    private Environment env;

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
            log.info("*********************** aopActive - {}", aopActive);
            return Stream.of(
                    Arguments.of("valid", autoFactory.make(), null),
                    //Arguments.of("id notNull", autoFactory.make(AutoDTOFactory.withId), "must be null"),
                    Arguments.of("id isNull", autoFactory.make(), null),
                    Arguments.of("username notNull", autoFactory.make().withUsername(autoFactory.username()),
                            aopActive ? "must be null" : null),
                    // Arguments.of("make isNull", autoFactory.make().withMake(null),
                    //         aopActive ? "must not be null" : null),
                    Arguments.of("make isNull", autoFactory.make().withMake("make"),
                               aopActive ? "must not be null" : null),
                    // Arguments.of("model isNull", autoFactory.make().withModel(null),
                    //         aopActive ? "must not be null" : null),
                    Arguments.of("model isNull", autoFactory.make().withModel("model"),
                            aopActive ? "must not be null" : null),
                    Arguments.of("passengers isNull", autoFactory.make().withPassengers(null),
                            aopActive ? "must not be null" : null),
                    // Arguments.of("dailyRate isNull", autoFactory.make().withDailyRate(null),
                    //         aopActive ? "must not be null" : null),
                    Arguments.of("dailyRate isNull", autoFactory.make().withDailyRate(new BigDecimal(15)),
                            aopActive ? "must not be null" : null),
                    // Arguments.of("fuelType isNull", autoFactory.make().withFuelType(null),
                    //         aopActive ? "must not be null" : null)
                    Arguments.of("fuelType isNull", autoFactory.make().withFuelType("fuel type"),
                            aopActive ? "must not be null" : null)
            );
        }

        
        @ParameterizedTest(name="{0}")
        @MethodSource("autos")
        void given(String name, AutoDTO autoDTO, String errorMsg) {
            //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> autosService.createAuto(autoDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                BDDAssertions.then(ex).isNull();
            } else {
                log.info("*********************** name - {} , errorMsg - {}", name, errorMsg);
                BDDAssertions.then(ex).as(()->"error not detected by dynamic proxy:" + autoDTO).isNotNull();
                log.info("{}", ex.getMessage());
                BDDAssertions.then(ex).hasMessageContaining(errorMsg);
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


        // Stream<Arguments> autos() {
        //     boolean aopActive = env.acceptsProfiles(Profiles.of("aop"));
        //     return Stream.of(
        //             Arguments.of("null id && username", autoFactory.make(), null),
        //             Arguments.of("id notNull", autoFactory.make(AutoDTOFactory.withId), null),
        //             Arguments.of("username notNull", autoFactory.make().withUsername(autoFactory.username()),
        //                     aopActive ? "must be null" : null),
        //             //Arguments.of("make isNull", autoFactory.make().withMake(null), null),
        //             Arguments.of("make notNull", autoFactory.make().withMake("make"), null),
        //             //Arguments.of("model isNull", autoFactory.make().withModel(null), null),
        //             Arguments.of("model notNull", autoFactory.make().withModel("model"), null),
        //             Arguments.of("passengers isNull", autoFactory.make().withPassengers(null), null),
        //             Arguments.of("dailyRate isNull", autoFactory.make().withDailyRate(new BigDecimal(15)), null),
        //             //Arguments.of("fuelType isNull", autoFactory.make().withFuelType(null), null)
        //             Arguments.of("fuelType notNull", autoFactory.make().withFuelType("fuelType"), null)
                    
        //     );
        // }

        Stream<Arguments> autos() {
            boolean aopActive = env.acceptsProfiles(Profiles.of("aop"));
            return Stream.of(
                    Arguments.of("null id && username", autoFactory.make(), null),
                    Arguments.of("id notNull", autoFactory.make(AutoDTOFactory.withId), null),
                    Arguments.of("username notNull", autoFactory.make().withUsername(autoFactory.username()),
                            aopActive ? "must be null" : null),
                    //Arguments.of("make isNull", autoFactory.make().withMake(null), null),
                    Arguments.of("make notNull", autoFactory.make().withMake("make"), null),
                    //Arguments.of("model isNull", autoFactory.make().withModel(null), null),
                    Arguments.of("model notNull", autoFactory.make().withModel("model"), null),
                    Arguments.of("passengers isNull", autoFactory.make().withPassengers(null), null),
                    Arguments.of("dailyRate isNull", autoFactory.make().withDailyRate(new BigDecimal(15)), null),
                    //Arguments.of("fuelType isNull", autoFactory.make().withFuelType(null), null)
                    Arguments.of("fuelType notNull", autoFactory.make().withFuelType("fuelType"), null)
                    
            );
        }


        @ParameterizedTest(name="{0}")
        @MethodSource("autos")
        void given(String name, AutoDTO autoDTO, String errorMsg) {
            AutoDTO auto = autosService.createAuto(autoFactory.make());
            //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> autosService.updateAuto(auto.getId(), autoDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                BDDAssertions.then(ex).isNull();
                AutoDTO resultingAuto = autosService.getAuto(auto.getId());
                BDDAssertions.then(resultingAuto).isEqualTo(autoDTO.withId(auto.getId()));
            } else {
                BDDAssertions.then(ex).as(()->"error not detected by dynamic proxy:" + autoDTO).isNotNull();
                log.info("{}", ex.getMessage());
                BDDAssertions.then(ex).hasMessageContaining(errorMsg);
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
                    //Arguments.of("id notNull", renterFactory.make(RenterDTOFactory.withId), "must be null"),
                    Arguments.of("id isNull", renterFactory.make(RenterDTOFactory.withId), null),
                    Arguments.of("username notNull", renterFactory.make().withUsername(renterFactory.username()),
                            aopActive ? "must be null" : null),
                    Arguments.of("email isNull", renterFactory.make().withEmail(null),
                            aopActive ? "must not be null" : null),
                    // Arguments.of("firstName isNull", renterFactory.make().withFirstName(null),
                    //         aopActive ? "must not be null" : null),
                    Arguments.of("firstName notNull", renterFactory.make().withFirstName("firstName"),
                            aopActive ? "must not be null" : null),
                    // Arguments.of("lastName isNull", renterFactory.make().withLastName(null),
                    //         aopActive ? "must not be null" : null)
                    Arguments.of("lastName notNull", renterFactory.make().withLastName("lastName"),
                            aopActive ? "must not be null" : null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("renters")
        void given(String name, RenterDTO renterDTO, String errorMsg) {
            //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> rentersService.createRenter(renterDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null == errorMsg) {
                BDDAssertions.then(ex).isNull();
            } else {
                log.info("******************** name - {}, errorMsg - {}", name, errorMsg);
                BDDAssertions.then(ex).as(() -> "error not detected by dynamic proxy:" + renterDTO).isNotNull();
                log.info("{}", ex.getMessage());
                BDDAssertions.then(ex).hasMessageContaining(errorMsg);
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
                    //Arguments.of("firstName isNull", renterFactory.make().withFirstName(null), null),
                    Arguments.of("firstName notNull", renterFactory.make().withFirstName("firstName"), null),
                    //Arguments.of("lastName isNull", renterFactory.make().withLastName(null), null)
                    Arguments.of("lastName isNull", renterFactory.make().withLastName("lastName"), null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("renters")
        void given(String name, RenterDTO renterDTO, String errorMsg) {
            RenterDTO renter = rentersService.createRenter(renterFactory.make());
            //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> rentersService.updateRenter(renter.getId(), renterDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                BDDAssertions.then(ex).isNull();
                RenterDTO resultingRenter = rentersService.getRenter(renter.getId());
                BDDAssertions.then(resultingRenter).isEqualTo(renterDTO.withId(renter.getId()));
            } else {
                BDDAssertions.then(ex).as(()->"error not detected by dynamic proxy:" + renterDTO).isNotNull();
                log.info("{}", ex.getMessage());
                BDDAssertions.then(ex).hasMessageContaining(errorMsg);
            }
        }
    }

}
