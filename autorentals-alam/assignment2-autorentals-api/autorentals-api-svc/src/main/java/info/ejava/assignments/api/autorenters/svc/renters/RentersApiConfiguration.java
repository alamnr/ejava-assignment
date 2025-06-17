package info.ejava.assignments.api.autorenters.svc.renters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidatorImpl;
import info.ejava.assignments.api.autorenters.svc.utils.RentersProperties;

@Configuration(proxyBeanMethods = false)
public class RentersApiConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("rentals.renters")
    public RentersProperties rentersProperties(){
        return new RentersProperties();
    }
    @Bean
    @ConditionalOnMissingBean
    public DtoValidator dtoValidator(){
        return new DtoValidatorImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public RenterDTORepository renterDTORepository(){
        return new RenterDTORepositoryMapImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public RenterService renterServiceMapImpl(RenterDTORepository renterDTORepository, DtoValidator dtoValidator, RentersProperties renterProps){
        return new RenterServiceImpl(renterDTORepository, dtoValidator, renterProps);
    }

    @Bean
    @ConditionalOnMissingBean
    public RentersController rentersController(RenterService renterService){
        return new RentersController(renterService);
    }

    @Bean
    public RentersExceptionAdvice rentersExceptionAdvice(){
        return new RentersExceptionAdvice();
    }
    
}
