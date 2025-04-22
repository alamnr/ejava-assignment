package info.ejava.alamnr.assignment1.testing.autorentals;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.assignments.testing.rentals.renters.RenterDTO;
import info.ejava.assignments.testing.rentals.renters.RenterValidator;
import info.ejava.assignments.testing.rentals.renters.RenterValidatorImpl;
import info.ejava.assignments.testing.rentals.renters.RentersConfiguration;
import info.ejava.assignments.testing.rentals.renters.RentersProperties;
import info.ejava.assignments.testing.rentals.renters.RentersService;
import info.ejava.assignments.testing.rentals.renters.RentersServiceImpl;

@TestConfiguration
public class RentersTestConfiguration {
    
    @Bean     
    @Qualifier("valid")
    public RenterDTO vallidRenter(){
        return RenterDTO.builder().firstName("warren").lastName("buffet")
                        .dob(LocalDate.of(1965,5,12)).build();
    }

    @Bean       
    @Qualifier("invalid")
    public RenterDTO invalidRenter(){
        return  RenterDTO.builder().firstName(null)
                                    .lastName("buffet")
                                    .dob(LocalDate.of(1999,5,25))
                                    .build();
    }

    // @Primary
    // @Bean
    // @ConfigurationProperties(prefix = "rentals.renters")
    // public RentersProperties rentersProp(){
    //     return new RentersProperties();
    // }

    // @Bean
    // @ConditionalOnProperty(prefix = "renters", name = "validatorMock", havingValue = "false")
    // public RenterValidator validator(){
    //     return new RenterValidatorImpl();
    // }

    // @Bean
    // public RentersService rentersService(@Autowired RentersProperties renterProps, 
    //                         @Autowired RenterValidator renterValidator){
    //     return new RentersServiceImpl(renterProps, renterValidator);

    // }
}
