package info.ejava.alamnr.assignment1.beanfactory.autorentals;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.AutoRentalsServiceImpl;
import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;

@Configuration
public class RentalsConfiguration {
    
    @Bean
    public RentalsService getRentalsService(){
        return new AutoRentalsServiceImpl();
    }

    @Bean 
    public AppCommand appCommand(RentalsService rentalsService){
        return new AppCommand(rentalsService);
    }
}
