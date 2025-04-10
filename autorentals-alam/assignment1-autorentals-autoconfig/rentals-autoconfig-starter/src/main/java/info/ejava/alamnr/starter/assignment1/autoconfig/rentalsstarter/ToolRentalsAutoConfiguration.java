package info.ejava.alamnr.starter.assignment1.autoconfig.rentalsstarter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;
import info.ejava.alamnr.assignment1.beanfactory.toolrentals.ToolRentalsServiceImpl;



@AutoConfiguration
//@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ToolRentalsServiceImpl.class)
public class ToolRentalsAutoConfiguration {
    
    @Bean
    public RentalsService toolRentalsService(){
        return new ToolRentalsServiceImpl();
    }
}
