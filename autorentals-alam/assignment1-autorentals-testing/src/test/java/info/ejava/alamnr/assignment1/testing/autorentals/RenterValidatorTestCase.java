package info.ejava.alamnr.assignment1.testing.autorentals;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import info.ejava.assignments.testing.rentals.renters.RenterDTO;
import info.ejava.assignments.testing.rentals.renters.RenterValidator;
import info.ejava.assignments.testing.rentals.renters.RenterValidatorImpl;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class RenterValidatorTestCase {

    private RenterValidator renterValidator;
    private RenterDTO validRenter;

    @BeforeEach
    void init () {
        renterValidator = new RenterValidatorImpl();        
    }

    @Test
    void validates_valid_registration() {
        // Arrange
        validRenter = RenterDTO.builder()
                .firstName("warren")
                .lastName("buffet")
                .dob(LocalDate.of(1930,8,30))
                .build();

        // Act
        List<String> errMsgs = renterValidator.validateNewRenter(validRenter, 20);

        // Assert
        Assertions.assertTrue(errMsgs.size()==0, "Expected valid registration to pass validation");

        Assertions.assertTrue(errMsgs.isEmpty());

        BDDAssertions.then(errMsgs.size()).as("ErrMsgs size should be 0").isEqualTo(0);
        BDDAssertions.then(errMsgs).as("ErrMsgs should be empty").isEmpty();

    }


    @Test
    void reports_blank_first_name() {
        // Arrange
        validRenter = RenterDTO.builder()
                .firstName("")
                .lastName("buffet")
                .dob(LocalDate.of(1930,8,30))
                .build();

        // Act
        List<String> errMsgs = renterValidator.validateNewRenter(validRenter, 20);

        // Assert
        Assertions.assertTrue(errMsgs.size()==1, "Expected blank first name to fail validation");
        System.out.println(errMsgs);
        Assertions.assertTrue(errMsgs.get(0).contains("renter.firstName"), "Expected error message to contain first name");
        
        BDDAssertions.then(errMsgs).as("ErrMsgs size should be 1").hasSize(1);

        BDDAssertions.and.then(errMsgs.get(0)).as("ErrMsgs should contain first name").contains("renter.firstName");

    }

    @Test
    void reports_blank_last_name(){
        // given /  Arrange  
        validRenter = RenterDTO.builder()
                        .firstName("Warren")
                        .lastName("")
                        .dob(LocalDate.of(1960, 2, 25))
                        .build();
        
        // when / act
        List<String> errMsgs = renterValidator.validateNewRenter(validRenter, 20);
        // assert
        Assertions.assertTrue(errMsgs.size()==1, "Error message size should be 1");
        Assertions.assertTrue(errMsgs.get(0).contains("renter.lastName"), "Error Message should contain renter.lastName");

        org.assertj.core.api.Assertions.assertThat(errMsgs.size()).as("error message size must be 1").isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(errMsgs.get(0)).as("don't contain message renter.lastName").contains("renter.lastName");


    }
}