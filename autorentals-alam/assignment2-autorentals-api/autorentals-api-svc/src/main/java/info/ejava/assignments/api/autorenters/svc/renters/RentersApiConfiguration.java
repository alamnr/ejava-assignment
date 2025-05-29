package info.ejava.assignments.api.autorenters.svc.renters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import info.ejava.assignments.api.autorenters.svc.utils.RenterValidator;
import info.ejava.assignments.api.autorenters.svc.utils.RenterValidatorImpl;
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
    public RenterValidator renterValidator(){
        return new RenterValidatorImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public RenterDTORepository renterDTORepository(){
        return new RenterDTORepositoryMapImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public RenterService renterServiceMapImpl(RenterDTORepository renterDTORepository, RenterValidator renterValidator, RentersProperties renterProps){
        return new RenterServiceImpl(renterDTORepository, renterValidator, renterProps);
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
