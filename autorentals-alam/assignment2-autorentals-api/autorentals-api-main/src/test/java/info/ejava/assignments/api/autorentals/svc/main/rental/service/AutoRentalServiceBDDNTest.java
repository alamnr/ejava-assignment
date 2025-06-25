package info.ejava.assignments.api.autorentals.svc.main.rental.service;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import info.ejava.assignments.api.autorentals.svc.main.rental.AutoRentalTestConfiguration;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalService;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = AutoRentalTestConfiguration.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRentalServiceBDDNTest {
    
    @Autowired @Qualifier("validAutoRental")
    private AutoRentalDTO validAutoRental;
    @Autowired @Qualifier("invalidAutoRental")
    private AutoRentalDTO invalidAutoRental;

    @Autowired
    private AutoRentalService  autoRentalService;

    @Test
    void can_create_valid_autoRental(){
        // given
        BDDAssertions.then(validAutoRental.getId()).isNull();
        // when
        AutoRentalDTO returnedAutoRentalDTO = autoRentalService.createAutoRental(validAutoRental);
        
        // then
        
        BDDAssertions.then(returnedAutoRentalDTO.getId()).isNotNull();
        BDDAssertions.then(returnedAutoRentalDTO.getId()).contains("autoRental-");
    }

    @Test
    void reject_invalid_autoRental() {
        // given

        BDDAssertions.then(invalidAutoRental.getId()).isNull();
        // when
        Throwable ex = BDDAssertions.catchThrowableOfType(ClientErrorException.InvalidInputException.class, 
                                                ()-> autoRentalService.createAutoRental(invalidAutoRental));

        // then
        
        BDDAssertions.then(ex.getMessage()).contains("autoRental.renterAge");
    }

}
