package info.ejava.assignments.api.autorentals.svc.main.auto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;


public class AutoTestConfiguration {

     @Bean
    @Qualifier("validAuto")
    public AutoDTO validAuto(){
        return  AutoDTO.builder().dailyRate(BigDecimal.valueOf(50.5))
                .fuelType("Gasolin")
                .location(StreetAddressDTO.builder().city("city-1")
                .state("state-1").street("street-1").zip("zip-1").build())
                .make("2020").model("2015").passengers(5).username("Mofig")
                .build();
    }
    
    // @Bean
    // @Qualifier("invalidRenter")
    // public AutoDTO invalidRenter(){
    //     return  AutoDTO.builder().email("valid@email.com").firstName("").lastName("Doe")
    //             .dob(LocalDate.of(1999,2,26)).build();
    // }

}