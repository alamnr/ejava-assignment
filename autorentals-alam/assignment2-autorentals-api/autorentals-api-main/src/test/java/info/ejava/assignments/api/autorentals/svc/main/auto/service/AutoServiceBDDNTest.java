package info.ejava.assignments.api.autorentals.svc.main.auto.service;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoTestConfiguration.class,AutoRentalsAppMain.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoServiceBDDNTest {
    
    @Autowired @Qualifier("validAuto")
    private AutoDTO validAutoDTO;
    @Autowired @Qualifier("invalidAuto")
    private AutoDTO invalidAutoDTO;

    @Autowired
    private AutosService  autosService;

    @Test
    void can_create_valid_autos(){
        // given
        BDDAssertions.then(validAutoDTO.getId()).isNull();
        // when
        log.info("valid auto - {} , \n{}",validAutoDTO, validAutoDTO.getDailyRate()==null || validAutoDTO.getDailyRate().doubleValue()<=0 );
        AutoDTO retuAutoDTO = autosService.createAuto(validAutoDTO);
        
        // then
        
        BDDAssertions.then(retuAutoDTO.getId()).isNotNull();
        BDDAssertions.then(retuAutoDTO.getId()).contains("auto-");
    }

    @Test
    void reject_invalid_auto() {
        // given

        BDDAssertions.then(invalidAutoDTO.getId()).isNull();
        // when
        Throwable ex = BDDAssertions.catchThrowableOfType(ClientErrorException.InvalidInputException.class, 
                                                ()-> autosService.createAuto(invalidAutoDTO));

        // then
        
        BDDAssertions.then(ex.getMessage()).contains("auto.model");
    }

}
