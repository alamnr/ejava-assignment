package info.ejava.assignments.api.autorentals.svc.main.renter;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RenterService;
import info.ejava.assignments.api.autorenters.svc.renters.RenterServiceImpl;

@TestConfiguration
public class RenterTestConfiguration {


    @Bean
    @Qualifier("validRenter")
    public RenterDTO validRenter(){
        return  RenterDTO.builder().email("valid@email.com").firstName("John").lastName("Doe")
                .dob(LocalDate.of(1930,2,26)).build();
    }
    
    @Bean
    @Qualifier("invalidRenter")
    public RenterDTO invalidRenter(){
        return  RenterDTO.builder().email("valid@email.com").firstName("Jane").lastName("Doe")
                .dob(LocalDate.of(2025,2,26)).build();
    }

    
}
