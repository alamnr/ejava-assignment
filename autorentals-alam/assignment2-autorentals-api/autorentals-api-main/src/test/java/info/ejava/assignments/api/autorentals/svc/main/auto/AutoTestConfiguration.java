package info.ejava.assignments.api.autorentals.svc.main.auto;

import java.math.BigDecimal;
import java.net.URI;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosJSONHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.client.autos.AutosXMLHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.client.renters.RentersJSONHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.examples.common.web.ServerConfig;
import info.ejava.examples.common.webflux.WebClientLoggingFilter;


public class AutoTestConfiguration {

     @Bean
    @Qualifier("validAuto")
    public AutoDTO validAuto(){
        return  AutoDTO.builder().dailyRate(BigDecimal.valueOf(50.5))
                .fuelType("Gasolin")
                .location(StreetAddressDTO.builder().city("city-1")
                .state("state-1").street("street-1").zip("zip-1").build())
                .make("2020").model("2015").passengers(5)
                .build();
    }
    
    @Bean
    @Qualifier("invalidAuto")
    public AutoDTO invalidAuto(){
        return  AutoDTO.builder().build();
    }

   

    @Bean @Lazy
    public ServerConfig serverConfig(@LocalServerPort int port) {
        return new ServerConfig().withPort(port).build();
    }

    @Bean @Lazy
    public URI baseUrl(ServerConfig serverConfig){
        return serverConfig.getBaseUrl();
    }

    @Bean @Lazy
    public URI autosUrl(URI baseUrl){
        return UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri();
    }

    @Bean
    public AutoDTOFactory autoDTOFactory() {
        return new AutoDTOFactory();
    }   

    @Bean 
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
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

    @Bean @Lazy @Qualifier("autosHttpIfaceJson")
    public AutosJSONHttpIfaceMapping autosAPIHttpIfaceJsonClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        
        RestClient restClient = RestClient.builder(restTemplate)
                .baseUrl(serverConfig.getBaseUrl().toString())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AutosJSONHttpIfaceMapping.class);
        
    }


    @Bean @Lazy @Qualifier("autosHttpIfaceXml")
    public AutosXMLHttpIfaceMapping autosAPIHttpIfaceXmlClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        
        RestClient restClient = RestClient.builder(restTemplate)
                .baseUrl(serverConfig.getBaseUrl().toString())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AutosXMLHttpIfaceMapping.class);
        
    }



}