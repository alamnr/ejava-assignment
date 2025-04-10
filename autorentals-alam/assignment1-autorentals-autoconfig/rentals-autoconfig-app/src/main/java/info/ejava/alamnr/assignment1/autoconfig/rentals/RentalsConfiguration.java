package info.ejava.alamnr.assignment1.autoconfig.rentals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;


//@AutoConfiguration
@Configuration(proxyBeanMethods = false)
public class RentalsConfiguration {

    @Bean
    public AppCommand appCommand(@Autowired(required = false) RentalsService rentalsService, 
                @Value("rentals.active:(not supplied)") String rentalsActive, 
                @Value("rentals.preference:(not supplied)") String rentalsPreference)
    {
       return new AppCommand(rentalsService, rentalsActive, rentalsPreference);
    }
    
}
