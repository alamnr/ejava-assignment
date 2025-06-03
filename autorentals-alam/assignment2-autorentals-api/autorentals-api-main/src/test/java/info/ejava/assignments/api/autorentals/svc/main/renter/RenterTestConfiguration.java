package info.ejava.assignments.api.autorentals.svc.main.renter;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.svc.renters.RenterService;
import info.ejava.assignments.api.autorenters.svc.renters.RenterServiceImpl;
import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;

@TestConfiguration
@Slf4j
public class RenterTestConfiguration {


    @Bean
    @Qualifier("validRenter")
    public RenterDTO validRenter(){
        return  RenterDTO.builder().email("valid@email.com").firstName("John").lastName("Doe")
                .dob(LocalDate.of(1930,2,26)).build();
    }
    
    @Bean
    @Qualifier("invalidRenter")
    public RenterDTO invalidRenter(){
        return  RenterDTO.builder().email("valid@email.com").firstName("").lastName("Doe")
                .dob(LocalDate.of(1999,2,26)).build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean @Lazy
    public ServerConfig serverConfig(@LocalServerPort int port) {
        return new ServerConfig().withPort(port).build();
    }

    @Bean @Lazy
    public URI baseUrl(ServerConfig serverConfig){
        return serverConfig.getBaseUrl();
    }

    
    
    @Bean   // injecting port way 3
    public RentersAPI rentersAPI(RestTemplate restTemplate){
        int port = 8080;
        String baseUrl = String.format("http://localhost:%d/",port);
        URI uri= UriComponentsBuilder.fromUriString(baseUrl).build().toUri();
        ServerConfig cfg = new ServerConfig().withBaseUrl(uri).build();
        return new RentersAPIClient(restTemplate, cfg, null);
    }

    @Bean
    public RenterDTOFactory renterDTOFactory() {
        return new RenterDTOFactory();
    }
    
}
