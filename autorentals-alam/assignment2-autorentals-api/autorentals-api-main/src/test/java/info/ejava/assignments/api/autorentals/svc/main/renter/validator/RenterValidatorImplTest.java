package info.ejava.assignments.api.autorentals.svc.main.renter.validator;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.utils.RenterValidator;
import info.ejava.assignments.api.autorenters.svc.utils.RenterValidatorImpl;
import lombok.extern.slf4j.Slf4j;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterValidatorImplTest {

    private RenterValidator renterValidator;
    private RenterDTO renterDTO;

    @BeforeEach
    public void init(){
        renterValidator = new RenterValidatorImpl();
    }

    @Test
    void validate_valid_renter() {
        // given / arrange
        renterDTO = RenterDTO.builder().email("valid@email.com")
                        .firstName("John").lastName("Doe")
                        .dob(LocalDate.of(1930,8,30)).build();

        // when / act 
        List<String> errMsgs = renterValidator.validateNewRenter(renterDTO, 20);

        // then / assert - evaluate
        Assertions.assertTrue(errMsgs.size()==0,"Error msg should be 0");
        Assertions.assertTrue(errMsgs.isEmpty(),"errMsg should be empty");

        BDDAssertions.then(errMsgs.size()).as("Error msg should be 0").isEqualTo(0);
        BDDAssertions.then(errMsgs).isEmpty();
    }

    @Test
    void reports_blank_first_name() {
        // given / arrange
        renterDTO  = RenterDTO.builder().lastName("Doe")
                    .dob(LocalDate.of(1930,8,30)).email("renter@email.com").build();
        
        // when / act
        List<String> errMsgs = renterValidator.validateNewRenter(renterDTO, 20);

        // then / evaluate - assert
        Assertions.assertTrue(errMsgs.size()==1);
        Assertions.assertTrue(!errMsgs.isEmpty());

        BDDAssertions.then(errMsgs.size()).isEqualTo(1);
        BDDAssertions.then(errMsgs).isNotEmpty();

        log.info("error - {}", errMsgs);
    }

    @Test
    void report_blank_last_name() {
        // given / arrange
        renterDTO = RenterDTO.builder().firstName("John")
                    .dob(LocalDate.of(1980, 5, 25)).email("renter@email.com").build();
        
        // when / act
        List<String> errMsg = renterValidator.validateNewRenter(renterDTO, 20);

        // then / evaluate -assert
        Assertions.assertTrue(errMsg.size()==1);
        Assertions.assertTrue(!errMsg.isEmpty());
        Assertions.assertEquals(errMsg.get(0), "renter.LastName - can not be blank");

        BDDAssertions.then(errMsg.size()).isEqualTo(1);
        BDDAssertions.then(errMsg).isNotEmpty();
        BDDAssertions.then(errMsg.get(0)).contains("renter.LastName");
    }
}
