package info.ejava.assignments.api.autorentals.svc.main.rental;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsAPI;
import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsJSONHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.client.autos.AutosXMLHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.examples.common.web.RestTemplateLoggingFilter;
import info.ejava.examples.common.web.ServerConfig;
import info.ejava.examples.common.webflux.WebClientLoggingFilter;
import lombok.extern.slf4j.Slf4j;

@TestConfiguration
@Slf4j
public class AutoRentalTestConfiguration {

    @Bean
    @Qualifier("validAutoRental")
    public AutoRentalDTO validAutoRental(){
        return AutoRentalDTO.builder().autoId("auto-1").renterId("renter-1")
                        .startDate(LocalDate.now()).endDate(LocalDate.now()).build();
    }
    
    @Bean
    @Qualifier("invalidAutoRental")
    public AutoRentalDTO invalidAutoRental(){
        return AutoRentalDTO.builder().autoId(" ").renterId(null)
                    .endDate(null).startDate(null).build();
    }

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
    @Qualifier("validRenter")
    public RenterDTO validRenter(){
        return  RenterDTO.builder().email("valid@email.com").firstName("John").lastName("Doe")
                .dob(LocalDate.of(1930,2,26)).build();
    }

    @Bean
    ClientHttpRequestFactory requestFactory(){
        return new SimpleClientHttpRequestFactory();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean @Qualifier("restTemplateWithLogger")
    public RestTemplate restTemplateWithLogger(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory ) {
        //return builder.build();
        // or just following will work in the simple cases like this 
        // return new RestTemplate();
        return builder.requestFactory(
              // used to read the Stream twice -- so we can use the logging filter below
              () -> new BufferingClientHttpRequestFactory(requestFactory))
              .interceptors(List.of(new RestTemplateLoggingFilter())).build();
        
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

    @Bean @Lazy
    public ServerConfig serverConfig(@LocalServerPort int port) {
        return new ServerConfig().withPort(port).build();
    }

    @Bean @Lazy
    public URI baseUrl(ServerConfig serverConfig){
        return serverConfig.getBaseUrl();
    }

    @Bean @Lazy
    public URI autoRentalUrl(URI baseUrl){
        return UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri();
    }


    @Bean
    public AutoRentalDTOFactory autoRentalDTOFactory() {
        return new AutoRentalDTOFactory();
    }

    @Bean @Lazy @Qualifier("autoRentalHttpIfaceJson")
    public AutoRentalsJSONHttpIfaceMapping autoRentalsJSONHttpIfaceMapping(RestTemplate restTemplate, ServerConfig serverConfig) {
        
        RestClient restClient = RestClient.builder(restTemplate)
                .baseUrl(serverConfig.getBaseUrl().toString())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AutoRentalsJSONHttpIfaceMapping.class);
        
    }


    @Bean @Lazy @Qualifier("autoRentalHttpIfaceXml")
    public AutosXMLHttpIfaceMapping autosAPIHttpIfaceXmlClient(RestTemplate restTemplate, ServerConfig serverConfig) {
        
        RestClient restClient = RestClient.builder(restTemplate)
                .baseUrl(serverConfig.getBaseUrl().toString())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AutosXMLHttpIfaceMapping.class);
        
    }

    
}
