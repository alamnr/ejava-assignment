package info.ejava.assignments.api.autorenters.svc.renters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
public class RentersApiConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RenterDTORepository renterDTORepository(){
        return new RenterDTORepositoryMapImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public RenterService renterServiceMapImpl(RenterDTORepository renterDTORepository){
        return new RenterServiceImpl(renterDTORepository);
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
