package info.ejava.assignments.api.autorenters.svc.autos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidatorImpl;

@Configuration(proxyBeanMethods = true)
public class AutosAPIConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AutosDTORepository autosDTORepository(){
        return new AutosDTORepositoryMapImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtoValidator dtoValidator () {
        return new DtoValidatorImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public AutosService AutosDTOService(AutosDTORepository repository, DtoValidator dtoValidator){
        return new AutoServiceImpl(repository, dtoValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutosController autosController(AutosService service){
        return new AutosController(service);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutosExceptionAdvice autosExceptionAdvice(){
        return new AutosExceptionAdvice();
    }

    
}
