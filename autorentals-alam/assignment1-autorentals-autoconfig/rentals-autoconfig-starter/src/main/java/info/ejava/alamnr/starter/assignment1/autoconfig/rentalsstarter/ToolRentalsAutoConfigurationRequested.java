package info.ejava.alamnr.starter.assignment1.autoconfig.rentalsstarter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;
import info.ejava.alamnr.assignment1.beanfactory.toolrentals.ToolRentalsServiceImpl;


@AutoConfiguration
//@Configuration(proxyBeanMethods = false)
//@AutoConfigureBefore(ToolRentalsAutoConfiguration.class)
@ConditionalOnClass(ToolRentalsServiceImpl.class)
//@ConditionalOnResource(resources = "file:./tools.properties")
@ConditionalOnProperty(prefix = "rentals", name = "preference", havingValue = "tools")
public class ToolRentalsAutoConfigurationRequested {
    
    @Bean
    public RentalsService tooRentalsRequested() {
        return new ToolRentalsServiceImpl();
    }
}
