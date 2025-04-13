package info.ejava.alamnr.assignment1.autorentals.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.alamnr.assignment1.autorentals.logging.app.AppCommand;
import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalsRepository;
import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalsRepositoryImpl;
import info.ejava.alamnr.assignment1.autorentals.logging.svc.AutoRentalsHelper;
import info.ejava.alamnr.assignment1.autorentals.logging.svc.AutoRentalsHelperImpl;
import info.ejava.alamnr.assignment1.autorentals.logging.svc.AutoRentalsService;
import info.ejava.alamnr.assignment1.autorentals.logging.svc.AutoRentalsServiceImpl;

@Configuration(proxyBeanMethods = false)
public class RentalsConfig {

    @Bean
    public AutoRentalsRepository repository(){
        return new AutoRentalsRepositoryImpl();
    }

    @Bean 
    public AutoRentalsHelper helper(){
        return new AutoRentalsHelperImpl(); 
    }

    @Bean
    public AutoRentalsService service(@Autowired AutoRentalsRepository repository, @Autowired AutoRentalsHelper helper) {
        return new AutoRentalsServiceImpl(helper, repository);
    }

    @Bean
    public AppCommand appCommand(@Autowired AutoRentalsService service){
        return new AppCommand(service);
    }
    
}
