package info.ejava.alamnr.assignment3.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.assignments.aop.autorenters.util.NullPropertyAssertion;
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import info.ejava.assignments.api.autorentals.svc.main.renter.RenterTestConfiguration;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes={AutoRentalsSecurityApp.class, AutoTestConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class MyD2_DynamnicProxyNTest_Extended {

    @Autowired
    private AutoDTOFactory autoFactory;
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired(required = false)
    private NullPropertyAssertion nullPropertyAssertion;
    @Autowired
    private AutosService autosService;
    @Autowired
    private RentersService rentersService;
    private static Method newInstanceFactoryMethod;

    @BeforeAll
    static void given() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("info.ejava.alamnr")
                .addScanners(Scanners.MethodsSignature));
        newInstanceFactoryMethod = reflections
                .getMethodsWithSignature(NullPropertyAssertion.class,
                        Object.class, String.class, List.class, List.class)
                .stream()
                .findFirst()
                .orElse(null);
    
        BDDAssumptions.given(newInstanceFactoryMethod)
                .as("could not locate newInstance factory method, check requied signature")
                .isNotNull();
        
    }

    @BeforeEach
    void given_bean() {
        BDDAssumptions.given(nullPropertyAssertion)
                .as("nullPropertyAssertion bean not injected; check App's component scan path")
                .isNotNull();
    
    }

    protected <T> T newProxyInstance(T target, String method, List<String> isNull, List<String> notNull)
            throws InvocationTargetException, IllegalAccessException {
                
        return (T) newInstanceFactoryMethod.invoke(null,nullPropertyAssertion, target, method, isNull, notNull);
    }

    
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class can_proxy_createAuto {
        private AutosService createAutoProxy;
        @BeforeAll
        void init() throws InvocationTargetException, IllegalAccessException {
            createAutoProxy = newProxyInstance(autosService, "createAuto",
                    List.of("id","username"), //isNull
                    List.of("make","model","passengers","dailyRate","fuelType")); //notNull
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user0","password"));

            log.info("************************************** creatAutoProxy - {}", createAutoProxy);
        }

        Stream<Arguments> autos() {
            return Stream.of(
                    Arguments.of("valid", autoFactory.make(), null),
                    Arguments.of("id notNull", autoFactory.make(AutoDTOFactory.withId), "must be null"),
                    Arguments.of("username notNull", autoFactory.make().withUsername(autoFactory.username()), "must be null"),
                    Arguments.of("make isNull", autoFactory.make().withMake(null), "must not be null"),
                    Arguments.of("model isNull", autoFactory.make().withModel(null), "must not be null"),
                    Arguments.of("passengers isNull", autoFactory.make().withPassengers(null), "must not be null"),
                    Arguments.of("dailyRate isNull", autoFactory.make().withDailyRate(null), "must not be null"),
                    Arguments.of("fuelType isNull", autoFactory.make().withFuelType(null), "must not be null")
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("autos")
        void given(String name, AutoDTO autoDTO, String errorMsg) {
            //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> createAutoProxy.createAuto(autoDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                BDDAssertions.then(ex).isNull();
            } else {
                BDDAssertions.then(ex).as(()->"error not detected by dynamic proxy:" + autoDTO).isNotNull();
                log.info("{}", ex.getMessage());
                BDDAssertions.then(ex).hasMessageContaining(errorMsg);
            }
        }
    }

    
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class can_proxy_updateAuto {
        private AutosService updateAutoProxy;
        @BeforeEach
        void init() throws InvocationTargetException, IllegalAccessException {
            updateAutoProxy = newProxyInstance(autosService, "updateAuto",
                    List.of("username"), //isNull
                    List.of()); //notNull
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user0","password"));

            log.info("************************************** updateAutoProxy - {}", updateAutoProxy);
        }
        
        @Test
        void test(){
        
            BDDAssertions.then(nullPropertyAssertion).isNotNull();
        }

        Stream<Arguments> autos() {
            return Stream.of(
                    Arguments.of("null id && username", autoFactory.make(), null),
                    Arguments.of("id notNull", autoFactory.make(AutoDTOFactory.withId), null),
                    Arguments.of("username notNull", autoFactory.make().withUsername(autoFactory.username()), null),
                    Arguments.of("make notNull", autoFactory.make().withMake("make me"), null),
                    Arguments.of("model notNull", autoFactory.make().withModel("2015"), null),
                    Arguments.of("passengers isNull", autoFactory.make().withPassengers(null), null),
                    Arguments.of("dailyRate notNull", autoFactory.make().withDailyRate(new BigDecimal(5.0)), null),
                    Arguments.of("fuelType notNull", autoFactory.make().withFuelType("Engine Oil"), null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("autos")
        void given(String name, AutoDTO autoDTO, String errorMsg) {
            AutoDTO auto = autosService.createAuto(autoFactory.make());
            
            //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> updateAutoProxy.updateAuto(auto.getId(), autoDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                BDDAssertions.then(ex).isNull();
                AutoDTO resultingAuto = updateAutoProxy.getAuto(auto.getId());
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
        private RentersService createRenterProxy;
        int index=1;
        @BeforeEach
        void init() throws InvocationTargetException, IllegalAccessException {
            createRenterProxy = newProxyInstance(rentersService, "createRenter",
                    List.of("id", "username"), //isNull
                    List.of("email", "firstName", "lastName")); //notNull
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user" + index++,"password"));
        }

        Stream<Arguments> renters() {
            return Stream.of(
                    Arguments.of("valid", renterFactory.make(), null),
                    Arguments.of("id notNull", renterFactory.make(RenterDTOFactory.withId), "must be null"),
                    Arguments.of("username notNull", renterFactory.make().withUsername(renterFactory.username()), "must be null"),
                    Arguments.of("email isNull", renterFactory.make().withEmail(null), "must not be null"),
                    Arguments.of("firstName isNull", renterFactory.make().withFirstName(null), "must not be null"),
                    Arguments.of("lastName isNull", renterFactory.make().withLastName(null), "must not be null")
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("renters")
        void given(String name, RenterDTO renterDTO, String errorMsg) {
                    //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> createRenterProxy.createRenter(renterDTO),
                    ClientErrorException.InvalidInputException.class);
            //then
            if (null==errorMsg) {
                BDDAssertions.then(ex).isNull();
            } else {
                BDDAssertions.then(ex).as(()->"error not detected by dynamic proxy:" + renterDTO).isNotNull();
                log.info("{}", ex.getMessage());
                BDDAssertions.then(ex).hasMessageContaining(errorMsg);
            }
        }
    }

/*
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class can_proxy_updateRenters {
        private RentersService updateRenterProxy;
        int index=1;
        @BeforeEach
        void init() throws InvocationTargetException, IllegalAccessException {
            updateRenterProxy = newProxyInstance(rentersService, "updateRenter",
                    List.of("username"), //isNull
                    List.of()); //notNull
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken("user" + index++,"password"));
        }

        Stream<Arguments> renters() {
            return Stream.of(
                    Arguments.of("null id & username", renterFactory.make(), null),
                    Arguments.of("id notNull", renterFactory.make(RenterDTOFactory.withId), null),
                    Arguments.of("username notNull", renterFactory.make().withUsername(renterFactory.username()), null),
                    Arguments.of("email isNull", renterFactory.make().withEmail(null), null),
                    Arguments.of("firstName notNull", renterFactory.make().withFirstName("kuddus"), null),
                    Arguments.of("lastName notNull", renterFactory.make().withLastName("abdul"), null)
            );
        }

        @ParameterizedTest(name="{0}")
        @MethodSource("renters")
        void given(String name, RenterDTO renterDTO, String errorMsg) {
            RenterDTO renter = rentersService.createRenter(renterFactory.make());
            //when
            ClientErrorException.InvalidInputException ex = Assertions.catchThrowableOfType(
                    () -> updateRenterProxy.updateRenter(renter.getId(), renterDTO),
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
    }   */
}
