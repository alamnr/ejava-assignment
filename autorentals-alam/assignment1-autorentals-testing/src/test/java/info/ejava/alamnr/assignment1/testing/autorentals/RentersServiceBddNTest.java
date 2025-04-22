package info.ejava.alamnr.assignment1.testing.autorentals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import info.ejava.assignments.testing.rentals.renters.RenterDTO;
import info.ejava.assignments.testing.rentals.renters.RentersService;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {RentersTestConfiguration.class},
                properties = "spring.main.allow-bean-definition-overriding=true")
//@ContextConfiguration(classes = RentersTestConfiguration.class) // do same  as above to import beans from RentersTestConfiguration.class
//@Import(RentersTestConfiguration.class) // do same  as above to import beans from RentersTestConfiguration.class
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("bdd_unit_integration_test")
@Slf4j
public class RentersServiceBddNTest {

    @Autowired
    private RentersService subject;
    @Autowired
    @Qualifier("valid")
    private RenterDTO validRenterDTO;

    @Autowired
    @Qualifier("invalid")
    private RenterDTO invalidRenterDTO;

    @Test
    void can_create_valid_renter(){
        // given / arrange


        // when /  act
            RenterDTO validReturnedRenter = subject.createRenter(validRenterDTO);
        // then / assert
        BDDAssertions.then(validRenterDTO.getId()).isNull();
        BDDAssertions.then(validReturnedRenter.getId()).isNotNull();
        BDDAssertions.then(validReturnedRenter.getId()).isEqualTo("1");
    }

    @Test
    void rejects_invalid_renter(){
        // given / arrange

        // when /act 
         Throwable ex = BDDAssertions.catchThrowableOfType(RuntimeException.class, 
                            ()-> subject.createRenter(invalidRenterDTO));

        // then / Assert

        BDDAssertions.then(invalidRenterDTO.getId()).isNull();
        BDDAssertions.then(ex.getMessage()).contains("invalid renter");
    }
}