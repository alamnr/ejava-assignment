package info.ejava.assignments.testing.rentals.renters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "renters", name = "active", havingValue = "true", matchIfMissing = true)
public class RentersConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("rentals.renters")
    public RentersProperties renterProperties() {
        return new RentersProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public RenterValidator renterValidator() {
        return new RenterValidatorImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public RentersService rentersService(RentersProperties props, RenterValidator validator) {
        return new RentersServiceImpl(props, validator);
    }
}
