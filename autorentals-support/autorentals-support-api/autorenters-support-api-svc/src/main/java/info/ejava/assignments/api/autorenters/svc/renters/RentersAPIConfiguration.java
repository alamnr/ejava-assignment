package info.ejava.assignments.api.autorenters.svc.renters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RentersAPIConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RentersDTORepository rentersDTORepository() {
        return new RentersDTORepositoryMapImpl();
    }

    @Bean
    //@Order(Ordered.LOWEST_PRECEDENCE)
    public RentersService rentersServiceDTOMapImpl(RentersDTORepository rentersDTORepository) {
        return new RentersServiceDTORepoImpl(rentersDTORepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RentersController rentersController(RentersService rentersService) {
        return new RentersController(rentersService);
    }

    @Bean
    public RentersExceptionAdvice rentersExceptionAdvice() {
        return new RentersExceptionAdvice();
    }
}
