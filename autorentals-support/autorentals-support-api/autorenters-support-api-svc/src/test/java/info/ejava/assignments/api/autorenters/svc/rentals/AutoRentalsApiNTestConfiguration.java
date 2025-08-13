package info.ejava.assignments.api.autorenters.svc.rentals;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.examples.common.web.RestTemplateConfig;
import info.ejava.examples.common.web.ServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * This class provides a base test configuration for performing
 * integration tests with Autos and Renters.
 */
@TestConfiguration(proxyBeanMethods = false)
public class AutoRentalsApiNTestConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AutoDTOFactory autoFactory() {
        return new AutoDTOFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public RenterDTOFactory renterFactory() {
        return new RenterDTOFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return new RestTemplateConfig().restTemplateDebug(builder);
    }

    @Bean @Lazy
    @ConditionalOnMissingBean
    public ServerConfig serverConfig(@LocalServerPort int port) {
        return new ServerConfig().withPort(port).build();
    }

    @Bean @Lazy
    @ConditionalOnMissingBean
    public AutosAPI autosAPI(RestTemplate restTemplate, ServerConfig serverConfig) {
        return new AutosAPIClient(restTemplate, serverConfig, MediaType.APPLICATION_JSON);
    }

    @Bean @Lazy
    @ConditionalOnMissingBean
    public RentersAPI rentersAPI(RestTemplate restTemplate, ServerConfig serverConfig) {
        return new RentersAPIClient(restTemplate, serverConfig, MediaType.APPLICATION_JSON);
    }
}
