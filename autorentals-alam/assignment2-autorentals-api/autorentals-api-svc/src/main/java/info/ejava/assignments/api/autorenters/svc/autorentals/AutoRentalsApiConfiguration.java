package info.ejava.assignments.api.autorenters.svc.autorentals;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.assignments.api.autorenters.svc.autos.AutosDTORepository;
import info.ejava.assignments.api.autorenters.svc.renters.RenterDTORepository;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidatorImpl;

@Configuration(proxyBeanMethods = false)
public class AutoRentalsApiConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DtoValidator dtoValidator(){
        return new DtoValidatorImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoRentalDTORepository autoRentalDTORepository(){
        return new AutoRentalDTORepositoryMapImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public AutoRentalService autoRentalService(AutoRentalDTORepository autoRentalDTORepository, AutosDTORepository autoRepository, 
                                                RenterDTORepository renterRepository, DtoValidator dtoValidator ){
        return new AutoRentalServiceImpl(autoRentalDTORepository,autoRepository, renterRepository, dtoValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoRentalController autoRentalController(AutoRentalService autoRentalService){
        return new AutoRentalController(autoRentalService);
    }

    @Bean
    public AutoRentalExceptionAdvice autoRentalExceptionAdvice(){
        return new AutoRentalExceptionAdvice();
    }
    
}
