package info.ejava.assignments.api.autorenters.svc;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.examples.common.web.RestTemplateConfig;
import info.ejava.examples.common.web.ServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@TestConfiguration(proxyBeanMethods = false)
public class ProvidedApiAutoRenterTestConfiguration {
    @Bean
    public AutoDTOFactory autoDTOFactory() {
        return new AutoDTOFactory();
    }

    @Bean
    public RenterDTOFactory renterDTOFactory() {
        return new RenterDTOFactory();
    }

    /**
     * A ServerConfig bean that is solely initialized by properties and has no dependency
     * on the test port#.
     * @return serverConfig instance that can be overwritten by configuration properties
     */
    @Bean
    @ConfigurationProperties("it.server")
    public ServerConfig baseServerConfig() {
        return new ServerConfig();
    }

    /**
     * A ServerConfig bean that is augmented with the runtime port #. This and all dependencies
     * of it must be @Lazy. It is not a ConfigurationProperties because we do not want the port#
     * overwritten.
     * @param baseServerConfig bean initialized by properties
     * @param port runtime port number
     * @return serverConfig updated with runtime port#
     */
    @Bean @Lazy
    public ServerConfig serverConfig(ServerConfig baseServerConfig, @LocalServerPort int port) {
        return baseServerConfig.withBaseUrl(null).withPort(port).build(); //force a re-build with runtime port setting
    }


    /**
     * Creates a vanilla HTTP request factory that is suitable for straight HTTP.
     * Set setting it.server.scheme=https to turn this off and define a custom one
     * based on HTTPS or whatever.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name="it.server.scheme", havingValue = "http", matchIfMissing = true)
    ClientHttpRequestFactory requestFactory() {
        return new SimpleClientHttpRequestFactory(); //no ssl concerns
    }

    /**
     * This creates a relatively vanilla, RestTemplate, with logging capability. Depending
     * on the supplied builder, it is for anonymous use.
     * @param builder to create the the RestTemplate from
     * @param requestFactory for built RestTemplate to use to create connections from
     * @return RestTemplate built from builder and augmented with logging.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory) {
        return new RestTemplateConfig().restTemplateDebug(builder, requestFactory);
    }


    /**
     * Returns a convenience bean for issuing commands to the autosService.
     * @param restTemplate to use for communications
     * @param serverConfig to use to establish URLs
     * @return client to issue commands
     */
    @Bean @Lazy
    public AutosAPIClient autosAPIClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        return new AutosAPIClient(restTemplate, serverConfig, MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a convenience bean for issuing commands to the rentersService.
     * @param restTemplate to use for communications
     * @param serverConfig to use to establish URLs
     * @return client to issue commands
     */
    @Bean @Lazy
    public RentersAPIClient rentersAPIClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        return new RentersAPIClient(restTemplate, serverConfig, MediaType.APPLICATION_JSON);
    }
}
