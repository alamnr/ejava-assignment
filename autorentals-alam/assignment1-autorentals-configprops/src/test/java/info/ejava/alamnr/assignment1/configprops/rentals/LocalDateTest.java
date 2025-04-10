package info.ejava.alamnr.assignment1.configprops.rentals;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LocalDateTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "2010-07-01",
       // "07-01-2010",
        //"07/01/2010"
    })    
    void can_parse(String value){
        LocalDate result = LocalDate.parse(value);
        System.out.println(value + " => " + result);
    }
}
