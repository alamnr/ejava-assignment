package info.ejava.assignments.api.autorenters.svc.autos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = true)
public class AutosAPIConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AutosDTORepository autosDTORepository() {
        return new AutosDTORepositoryMapImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public AutosService autosDTOService(AutosDTORepository rentersRepository) {
        return new AutosServiceDTORepoImpl(rentersRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutosController autosController(AutosService autosService) {
        return new AutosController(autosService);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutosExceptionAdvice autosExceptionAdvice() {
        return new AutosExceptionAdvice();
    }
}
