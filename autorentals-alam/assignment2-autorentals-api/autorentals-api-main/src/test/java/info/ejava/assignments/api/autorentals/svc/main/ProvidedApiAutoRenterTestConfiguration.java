package info.ejava.assignments.api.autorentals.svc.main;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.client.renters.RentersHttpIface;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIRestClient;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIRestTemplate;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIWebClient;
import info.ejava.assignments.api.autorenters.client.autos.AutosJSONHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.client.autos.AutosXMLHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.examples.common.web.RestTemplateConfig;
import info.ejava.examples.common.web.ServerConfig;
import info.ejava.examples.common.webflux.WebClientLoggingFilter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties.Restclient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@TestConfiguration (proxyBeanMethods = false)
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


        @Bean 
    public RestClient restClient(RestClient.Builder builder, RestTemplate restTemplate){
        return builder.build();
        // return RestClient.create(restTemplate);
                        
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        // return builder.build();
        // or just following will work in the simple cases like this
        // return WebClient.builder().build();
        return builder.filter(WebClientLoggingFilter.requestFilter())
                        .filter(WebClientLoggingFilter.responseFilter())
                            .build();
    }



    /**
     * Returns a convenience bean for issuing commands to the autosService.
     * @param restTemplate to use for communications
     * @param serverConfig to use to establish URLs
     * @return client to issue commands
     */
    @Bean @Lazy @Qualifier("autosRestTemplate")
    public AutosAPI autosAPIRestTemplateClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        return new AutosAPIRestTemplate(restTemplate, serverConfig, MediaType.APPLICATION_JSON);
    }

    @Bean @Lazy @Qualifier("autosRestClient")
    public AutosAPI autosAPIRestClient(RestClient restClient, ServerConfig serverConfig) {
        return new AutosAPIRestClient(restClient, serverConfig, MediaType.APPLICATION_JSON);
    }

    @Bean @Lazy @Qualifier("autosWebClient")
    public AutosAPI autosAPIWebClient(WebClient webClient, ServerConfig serverConfig) {
        return new AutosAPIWebClient(webClient, serverConfig, MediaType.APPLICATION_JSON);
    }
    

    @Bean @Lazy @Qualifier("autosHttpIfaceJson")
    public AutosJSONHttpIfaceMapping autosAPIHttpIfaceJsonClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        
        final RestClient restClient =  RestClient.builder(restTemplate)
                                        .baseUrl(serverConfig.getBaseUrl())
                                        .build();
        final RestClientAdapter adapter = RestClientAdapter.create(restClient);
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        final AutosJSONHttpIfaceMapping autosJSONHttpIfaceMapping = factory.createClient(AutosJSONHttpIfaceMapping.class);
        return autosJSONHttpIfaceMapping;
    }

    @Bean @Lazy @Qualifier("autosHttpIfaceXml")
    public AutosXMLHttpIfaceMapping autosAPIHttpIfaceXmlClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        final RestClient restClient =  RestClient.builder(restTemplate)
                                        .baseUrl(serverConfig.getBaseUrl())
                                        .build();
        final RestClientAdapter adapter = RestClientAdapter.create(restClient);
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        final AutosXMLHttpIfaceMapping autosXMLHttpIfaceMapping = factory.createClient(AutosXMLHttpIfaceMapping.class);
        return autosXMLHttpIfaceMapping;
    }

    /**
     * Returns a convenience bean for issuing commands to the rentersService.
     * @param restTemplate to use for communications
     * @param serverConfig to use to establish URLs
     * @return client to issue commands
     */
    @Bean @Lazy @Qualifier("rentersHttpIfaceJson")
    public RentersAPIClient rentersAPIHttpIfaceJsonClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        return new RentersAPIClient(restTemplate, serverConfig, MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
    }

    @Bean @Lazy @Qualifier("rentersHttpIfaceXml")
    public RentersAPIClient rentersAPIHttpIfaceXmlClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        return new RentersAPIClient(restTemplate, serverConfig, MediaType.valueOf(MediaType.APPLICATION_XML_VALUE));
    }

    
}
