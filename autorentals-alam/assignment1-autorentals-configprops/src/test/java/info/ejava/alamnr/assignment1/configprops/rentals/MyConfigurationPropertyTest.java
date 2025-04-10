package info.ejava.alamnr.assignment1.configprops.rentals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * This test does a light verification of the populated PropertyPrinter component.
 * It uses reflection because all of the data types are in the starter project
 * and it is unknown what Java packaging things will use.
 *
 * We will cover testing soon and reflection several weeks after that
 */

 @SpringBootTest
public class MyConfigurationPropertyTest {
    
    private Object component;
    @BeforeEach
    void init(@Autowired ApplicationContext ctx) throws Exception {
        component = ctx.getBean("propertyPrinter");
    }

     @Test
    void component_has_autorentals() throws Exception {
        List<?> autos = get(component, "autos", List.class);
        BDDAssertions.assertThat(autos).as("autos not injected").isNotNull();
        BDDAssertions.assertThat(autos).as("autos").hasSize(3);
        BDDAssertions.assertThat(autos)
                .extracting(object->getId(object))
                .as("autoId")
                .containsAll(List.of(1,4,5));

        Object rental = autos.stream()
                .filter(s->((Integer)getId(s)).equals(1))
                .findFirst()
                .orElseThrow();

        BDDAssertions.assertThat(get(rental, "renterName", String.class)).as(()->context(rental)+".renterName").isEqualTo("Joe Camper");
        BDDAssertions.assertThat(get(rental, "rentalDate", LocalDate.class)).as(()->context(rental)+".rentalDate").isEqualTo(LocalDate.of(2010,7,1));
        BDDAssertions.assertThat(get(rental, "rentalAmount", BigDecimal.class)).as(()->context(rental)+".rentalAmount").isEqualTo(BigDecimal.valueOf(100.00));
        Object location = get(rental, "location", Object.class);
        BDDAssertions.assertThat(location).as(()->context(rental)+".location").isNotNull();
        BDDAssertions.assertThat(get(location, "city", String.class)).as(()->context(rental) + ".location.city").isNotNull();
        BDDAssertions.assertThat(get(location, "state", String.class)).as(()->context(rental)+".location.state").isNotNull();
    }


    private String context(Object rental) {
        return String.format("rental[%d]", getId(rental));
    }


    @Test
    void component_has_tool_rentals() throws Exception {
        List<?>  tools = get(component, "tools", List.class);
        BDDAssertions.assertThat(tools).as("tools not injected").isNotNull();
        BDDAssertions.assertThat(tools).as("tools").hasSize(1);
        BDDAssertions.assertThat(tools)
                        .extracting(obj -> getId(obj))
                        .as("autoId")
                        .containsAll(List.of(2));
    }

    @Test
    void component_has_boat_rental() throws Exception {
        Object boat = get(component, "boat", Object.class);
        BDDAssertions.assertThat(boat).as("boat is not injected").isNotNull();
        BDDAssertions.assertThat(getId(boat)).as("boatId").isEqualTo(3);
    }

    private int getId(Object obj) {
        return get(obj, "id", Integer.class);
    }
    private <T> T get(Object obj, String property, Class<T> type) {
        return (T) ReflectionTestUtils.invokeGetterMethod(obj, property);
//        String getter = "get" + StringUtils.capitalize(property);
//        try {
//            Method getId = obj.getClass().getDeclaredMethod(getter);
//            return (T) getId.invoke(obj);
//        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
//            throw new RuntimeException(ex);
//        }
    }

}
