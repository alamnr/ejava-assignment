package info.ejava.assignments.api.autorentals.svc.main.renter.service;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import info.ejava.assignments.api.autorentals.svc.main.renter.RenterTestConfiguration;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = RenterTestConfiguration.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterServiceBDDNTest {
    
    @Autowired @Qualifier("validRenter")
    private RenterDTO validRenterDTO;
    @Autowired @Qualifier("invalidRenter")
    private RenterDTO invalidRenterDTO;

    @Autowired
    private RentersService  renterService;

    @Test
    void can_create_valid_renter(){
        // given
        BDDAssertions.then(validRenterDTO.getId()).isNull();
        // when
        RenterDTO retuRenterDTO = renterService.createRenter(validRenterDTO);
        
        // then
        
        BDDAssertions.then(retuRenterDTO.getId()).isNotNull();
        BDDAssertions.then(retuRenterDTO.getId()).contains("renter-");
    }

    @Test
    void reject_invalid_renter() {
        // given

        BDDAssertions.then(invalidRenterDTO.getId()).isNull();
        // when
        Throwable ex = BDDAssertions.catchThrowableOfType(ClientErrorException.InvalidInputException.class, 
                                                ()-> renterService.createRenter(invalidRenterDTO));

        // then
        
        BDDAssertions.then(ex.getMessage()).contains("renter.FirstName");
    }

}
