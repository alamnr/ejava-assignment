package info.ejava.assignments.api.autorenters.svc.autos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = true)
public class AutosAPIConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AutosDTORepository autosDTORepository(){
        return new AutosDTORepositoryMapImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public AutosService AutosDTOService(AutosDTORepository repository){
        return new AutoServiceImpl(repository);
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
