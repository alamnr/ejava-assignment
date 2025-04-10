package info.ejava.alamnr.starter.assignment1.autoconfig.rentalsstarter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.AutoRentalsServiceImpl;
import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;

@AutoConfiguration
//@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AutoRentalsServiceImpl.class)
@ConditionalOnProperty(prefix = "rentals" , name = "active", havingValue = "true")
public class AutoRentalsAutoConfiguration {

    @Bean
    public RentalsService autoRentalsService() {
        return new AutoRentalsServiceImpl();
    }
}
