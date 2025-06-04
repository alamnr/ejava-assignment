package info.ejava.assignments.api.autorentals.svc.main.renter.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.renter.RenterTestConfiguration;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoRentalsAppMain.class,RenterTestConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterRestTemplateClientNTest {
    
    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;  // injecting port way -1

    @Autowired
    private URI baseUrl;

    @Autowired
    private RenterDTOFactory renterDTOFactory;

    @Autowired @Qualifier("validRenter")
    private RenterDTO validRenter;

    @Autowired @Qualifier("invalidRenter")
    private RenterDTO invalidRenter;

        
    private static final MediaType[] MEDIA_TYPES = new MediaType[] {
           MediaType.APPLICATION_JSON,
           MediaType.APPLICATION_XML
    };

    public static Stream<Arguments> mediaTypes() {
        List<Arguments> params = new ArrayList<>();
        for (MediaType  contentType  : MEDIA_TYPES) {            
            for (MediaType acceptType : MEDIA_TYPES) {
                params.add(Arguments.of(contentType,acceptType));
            }
        }
        return params.stream();
    }

    public MessageDTO getErrorResponse(RestClientResponseException ex){
        final String contentTypeValue = ex.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final MediaType contentType = MediaType.valueOf(contentTypeValue);
        final byte[] bytes = ex.getResponseBodyAsByteArray();
        if (MediaType.APPLICATION_JSON.equals(contentType)) {
            return JsonUtil.instance().unmarshal(bytes, MessageDTO.class);
        } else if (MediaType.APPLICATION_XML.equals(contentType)) {
            return JsonUtil.instance().unmarshal(bytes, MessageDTO.class);
        } else {
            throw new IllegalArgumentException("unknown contentType: " + contentTypeValue);
        }


    }


    @BeforeEach  // injecting port way -2
    public void init(@LocalServerPort int port ) {
        //log.info("port way2 - {}", port);
        restTemplate.delete(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH).build().toUri()) ;         
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_renter_for_type(MediaType contentType, MediaType accept){

        // given - a valid quote
      
        RenterDTO validRenter = renterDTOFactory.make();
        log.info("Content-Type-{}, Accept-Type-{}, quote -{}", contentType, accept, validRenter);

        // when - making a request with different content and accept payload types
        RequestEntity<RenterDTO> request = RequestEntity
                                                .post(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH).build().toUri())
                                                .contentType(contentType)
                                                .accept(accept)
                                                .body(validRenter);

        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        ResponseEntity<RenterDTO> response = restTemplate.exchange(request, RenterDTO.class);

        log.info("resp. body - {}", response.getBody());
        log.info("resp. content type - {}", response.getHeaders().getContentType());
        
        // then - the service will accept the format we supplied
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BDDAssertions.then(response.getHeaders().getContentType()).isEqualTo(accept);
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo(accept.toString());

        // that equals what we sent and plus an ID generated
        RenterDTO createdRenter = response.getBody();
        BDDAssertions.then(createdRenter).isEqualTo(validRenter.withId(createdRenter.getId()));
        // with a location reponse header referencing the URI of the created quote
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(createdRenter.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());

    }



}
