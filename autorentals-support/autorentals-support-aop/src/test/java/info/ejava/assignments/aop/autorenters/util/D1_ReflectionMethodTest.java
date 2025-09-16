package info.ejava.assignments.aop.autorenters.util;


import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssumptions.given;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class D1_ReflectionMethodTest {
    private static AutoDTOFactory autoFactory = new AutoDTOFactory();
    private static RenterDTOFactory renterFactory = new RenterDTOFactory();
    protected NullPropertyAssertion nullPropertyAssertion;

    @BeforeEach
    void init() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("info.ejava.alamnr");
        Class<?> implClass = reflections
                .getSubTypesOf(NullPropertyAssertion.class).stream()
                .findFirst()
                .orElse(null);

        given(implClass)
                .as("impl not found, is it under info.ejava.alamnr?")
                .isNotNull();
        nullPropertyAssertion = (NullPropertyAssertion) implClass.getConstructor().newInstance();
    }

    static Stream<Arguments> isNull() {
        return Stream.of(
                Arguments.of(new Object(), "id"),
                Arguments.of(autoFactory.make(), "id"),
                Arguments.of(autoFactory.make(), "username"),
                Arguments.of(renterFactory.make(), "id"),
                Arguments.of(renterFactory.make(), "username")
        );
    }
    static Stream<Arguments> isNotNull() {
        return Stream.of(
                Arguments.of("hello, world", "bytes"),
                Arguments.of(autoFactory.make(), "value"),
                Arguments.of(autoFactory.make(), "make"),
                Arguments.of(autoFactory.make(), "model"),
                Arguments.of(autoFactory.make(), "passengers"),
                Arguments.of(renterFactory.make(), "email"),
                Arguments.of(renterFactory.make(), "firstName"),
                Arguments.of(renterFactory.make(), "lastName")
        );
    }

    @ParameterizedTest(name = "{1} is null")
    @MethodSource("isNull")
    void can_identify_missing_nulls(/*given*/Object object, String property) {
        //then
        assertDoesNotThrow(()-> nullPropertyAssertion.assertNull(object, property));
    }

    @Test
    void can_identify_null_violations() {
        //given
        Object object = renterFactory.make(); //factory always makes renter with firstName
        //when
        Exception ex = assertThrows(RuntimeException.class,
                ()-> nullPropertyAssertion.assertNull(object, "firstName"));
        //then
        then(ex).hasMessageContaining("must be null");
    }

    @ParameterizedTest(name = "{1} is not null")
    @MethodSource("isNotNull")
    void can_identify_non_nulls(/*given*/Object object, String property) {
        //then
        assertDoesNotThrow(()-> nullPropertyAssertion.assertNotNull(object, property));
    }

    @Test
    void can_identify_non_null_violations() {
        //given
        Object object = autoFactory.make(); //factory always makes auto with null id
        //when
        RuntimeException ex = assertThrows(RuntimeException.class,
                ()-> nullPropertyAssertion.assertNotNull(object, "id"));
        //then
        then(ex).hasMessageContaining("must not be null");
    }
}
