package info.ejava.assignments.api.autorentals.svc.main.renter.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.renter.RenterTestConfiguration;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {RenterTestConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterWebClientNTest {
     @Autowired //@Qualifier("restTemplateWithLogger")
    private WebClient webClient;

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

    @Autowired
    private URI rentersUrl;

        
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

    public MessageDTO getErrorResponse(WebClientResponseException ex){
        final String contentTypeValue = ex.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
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
        webClient.delete().uri(rentersUrl).retrieve().toEntity(Void.class).block() ;         
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_renter_for_type(MediaType contentType, MediaType accept){

        // given - a valid renter
      
       // RenterDTO validRenter = renterDTOFactory.make();
        log.info("Content-Type-{}, Accept-Type-{}, renter -{}", contentType, accept, validRenter);

        // when - making a request with different content and accept payload types
        RequestEntity<RenterDTO> request = RequestEntity
                                            .post(rentersUrl)
                                            .contentType(contentType)
                                            .accept(accept)
                                            .body(validRenter);

        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        
        ResponseEntity<RenterDTO> response = webClient.post().uri(rentersUrl)
                                            .contentType(contentType).accept(accept)
                                            .bodyValue(validRenter).retrieve()
                                            .toEntity(RenterDTO.class).block();

        log.info("resp. body - {}", response.getBody());
        log.info("resp. content type - {}", response.getHeaders().getContentType());
        
        // then - the service will accept the format we supplied
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BDDAssertions.then(response.getHeaders().getContentType()).isEqualTo(accept);
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo(accept.toString());

        // that equals what we sent and plus an ID generated
        RenterDTO createdRenter = response.getBody();
        BDDAssertions.then(createdRenter).isEqualTo(validRenter.withId(createdRenter.getId()));
        // with a location reponse header referencing the URI of the created renter
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(createdRenter.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());
        
    }

    @Test
    void get_renter(){
        // given/ arrange - an existing renter
        
        
        ResponseEntity<RenterDTO> response = webClient.post()
                                            .uri(rentersUrl).bodyValue(validRenter)
                                            .retrieve().toEntity(RenterDTO.class).block();
        BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();        

        String requestId = response.getBody().getId();
        
        URI renterUrl = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(requestId);
        
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(renterUrl);
        
        // when / act - requesting renter get by id
        ResponseEntity<RenterDTO> renterResponse  = webClient.get().uri(renterUrl).retrieve().toEntity(RenterDTO.class).block();
        
        // then
        BDDAssertions.then(renterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(renterResponse.getBody()).isEqualTo(validRenter.withId(requestId));

    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void get_renters(String mediaTypeString){
        // given / arrange
        log.info("mediaTypeString - {}", mediaTypeString);
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String,RenterDTO> existingRenters = new HashMap<>();
        RenterListDTO renters = renterDTOFactory.listBuilder().make(40, 40);
        for (RenterDTO renter : renters.getRenters()) {
            ResponseEntity<RenterDTO> response = webClient.post().uri(rentersUrl).contentType(mediaType)
                                                .bodyValue(renter).retrieve().toEntity(RenterDTO.class).block();
            BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            RenterDTO addedRenter = response.getBody();
            existingRenters.put(addedRenter.getId(), addedRenter);
        }
        BDDAssertions.assertThat(existingRenters).isNotEmpty();
        URI rentersUri = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH).build().toUri();
        URI rentersUriWithpageNumberAndpageSize = UriComponentsBuilder.fromUri(baseUrl)
                                        .path(RentersAPI.RENTERS_PATH)
                                        .queryParam("pageNumber", 1)
                                        .queryParam("pageSize", 20)
                                        .build().toUri();

        // when / act
        
        ResponseEntity<RenterListDTO> response = webClient.get().uri(rentersUri)
                                                .accept(mediaType).retrieve().toEntity(RenterListDTO.class).block();
        
        ResponseEntity<RenterListDTO> responseWithpageNumberAndpageSize = webClient.get().uri(rentersUriWithpageNumberAndpageSize)
                                                                    .accept(mediaType).retrieve().toEntity(RenterListDTO.class).block();
                                            

        // then / evaluate and assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithpageNumberAndpageSize.getStatusCode()).isEqualTo(HttpStatus.OK);
        RenterListDTO renterPageWithoutpageNumberAndpageSize = response.getBody();
        RenterListDTO renterPageWithpageNumberAndpageSize = responseWithpageNumberAndpageSize.getBody();

        
        BDDAssertions.then(renterPageWithoutpageNumberAndpageSize.getOffset()).isEqualTo(0);
        BDDAssertions.then(renterPageWithoutpageNumberAndpageSize.getLimit()).isEqualTo(0);
        BDDAssertions.then(renterPageWithpageNumberAndpageSize.getOffset()).isEqualTo(1);
        BDDAssertions.then(renterPageWithpageNumberAndpageSize.getLimit()).isEqualTo(20);

        BDDAssertions.then(renterPageWithoutpageNumberAndpageSize.getCount()).isEqualTo(existingRenters.size());
        BDDAssertions.then(renterPageWithpageNumberAndpageSize.getCount()).isEqualTo(20);
        
         for (RenterDTO q: renterPageWithoutpageNumberAndpageSize.getRenters()) {
            BDDAssertions.then(existingRenters.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(existingRenters).isEmpty();

    }

    
    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_renter(MediaType contentType, MediaType accept) {
        // given / arrange - a valid quote
                                                            
        // when / act 
        ResponseEntity<RenterDTO> response = webClient.post().uri(rentersUrl)
                                            .contentType(contentType).bodyValue(validRenter)
                                            .retrieve().toEntity(RenterDTO.class).block();
        
        // then 
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // that equals what we sent plus an id generated
        
        RenterDTO createdQuote = response.getBody();
        BDDAssertions.then(createdQuote).isEqualTo(validRenter.withId(createdQuote.getId()));
        // a lcation response header referencing the URL for the created resources
        URI location = UriComponentsBuilder.fromUri(baseUrl) .path(RentersAPI.RENTER_PATH).build(createdQuote.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).as("location error").isEqualTo(location.toString());
    }

    private RenterDTO given_an_existing_renter(){
        RenterDTO existingRenter = renterDTOFactory.make();
        ResponseEntity<RenterDTO> response =  webClient.post().uri(rentersUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .bodyValue(existingRenter)
                                                            .retrieve().toEntity(RenterDTO.class).block();
        
        BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void update_existing_quote( ){
        // given - an existing quote
        RenterDTO existingRenter = given_an_existing_renter();
        String requestId = existingRenter.getId();

        // and an update 
        RenterDTO updatedRenter = existingRenter.withFirstName(existingRenter.getFirstName() + "Updated ");

        URI updateUri = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(existingRenter.getId());

        // when - updating existing quote
        ResponseEntity<Void> response = webClient.put().uri(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(updatedRenter)
                                                        .retrieve().toEntity(Void.class).block();
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(requestId);
        ResponseEntity<RenterDTO> getUpdatedQuote = webClient.get().uri(getUri).retrieve().toEntity(RenterDTO.class).block();

        BDDAssertions.then(getUpdatedQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedQuote.getBody()).isEqualTo(updatedRenter);
        BDDAssertions.then(getUpdatedQuote.getBody()).isNotEqualTo(existingRenter);

    }

    @Test
    void get_renter_1() {
        // given / arrange
        RenterDTO existingRenter = renterDTOFactory.make();
        ResponseEntity<RenterDTO> response = webClient.post().uri(rentersUrl)
                                            .bodyValue(existingRenter).retrieve().toEntity(RenterDTO.class).block();
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act

        ResponseEntity<RenterDTO> getRenter =  webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                                 .path(RentersAPI.RENTER_PATH).build(requestId))
                                                 .retrieve().toEntity(RenterDTO.class).block();
        

        // then
        BDDAssertions.then(getRenter.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getRenter.getBody()).isEqualTo(existingRenter.withId(requestId));
    }

    protected List<RenterDTO> given_many_renters(int count) {
        List<RenterDTO> renters = new ArrayList<>(count);
        for (RenterDTO renterDTO : renterDTOFactory.listBuilder().renters(count,count)) {
                ResponseEntity<RenterDTO> response = webClient.post().uri(rentersUrl).bodyValue(renterDTO).retrieve().toEntity(RenterDTO.class).block();
                
                BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                renters.add(response.getBody());
        }
        return renters;
    }

    @Test
    void remove_renter(){
        // given 
        List<RenterDTO> renters = given_many_renters(5);
        String requestId = renters.get(1).getId();
        BDDAssertions.assertThat(webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(requestId))
                                          .retrieve().toEntity(RenterDTO.class).block().getStatusCode()).isEqualTo(HttpStatus.OK);
            
        // when requested to remove
        ResponseEntity<Void> resp = webClient.delete().uri(UriComponentsBuilder
                                            .fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(requestId))
                                            .retrieve().toEntity(Void.class).block();
        
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        URI u = UriComponentsBuilder.fromUri(baseUrl)
                                            .path(RentersAPI.RENTER_PATH).build(requestId);
        log.info("requestUri - {}, requiestId -{}", u.toString(), requestId);
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(RentersAPI.RENTER_PATH).build(requestId)).retrieve().toEntity(RenterDTO.class).block(),
                                             WebClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void remove_all_renters() {
        // given  / arrange
        List<RenterDTO> renters = given_many_renters(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = webClient.delete().uri(rentersUrl).retrieve().toEntity(Void.class).block();
                                           
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (RenterDTO renterDTO : renters) {
               WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()-> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH)
                        .build(renterDTO.getId())).retrieve().toEntity(RenterDTO.class).block(),
                         WebClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


    @Test
    void remove_unknown_renter() {
        // given 
        String requestId = "renter-13";

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = webClient.delete().uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(requestId))
                                        .retrieve().toEntity(Void.class).block();
                                                
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }


    @Test
    void get_random_renter_no_renters(){
        // given
        BDDAssertions.assertThat( webClient.delete().uri(rentersUrl)
                        .retrieve().toEntity(Void.class).block().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
               
        // then
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build("renter-1"))
                            .retrieve().toEntity(RenterDTO.class).block(),
                             WebClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains("Renter-[renter-1] not found");
        
    }

    @Test
    void get_unknown_renter(){
        // given
        String unknownId =  "renter-13";

        // when - requesting quote by id

        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(unknownId))
                                    .retrieve().toEntity(RenterDTO.class).block(),WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("Renter-[%s] not found", unknownId));

    }


    @Test
    void update_unknown_renter() {
        // given

        String unknownId = "renter-13";
        RenterDTO updateRenter = renterDTOFactory.make();

        // verify that updating existing quote
        WebClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()->  webClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(unknownId))
                            .bodyValue(updateRenter).retrieve().toEntity(Void.class).block(), WebClientResponseException.class);

        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("renter-[%s] not found", unknownId));
    }

    @Test
    //@Disabled
    void update_known_renter_with_bad_renter() {
        // given
        List<RenterDTO> renters = given_many_renters(3);
        
        String unknownId = "renter-000";
        RenterDTO badRenterMissingText = new RenterDTO();
        badRenterMissingText.withId(unknownId);

        // ResponseEntity<RenterDTO> resp = restTemplate.exchange(
        //         RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(unknownId)).build()
        //             ,RenterDTO.class);
        // log.info("resp - {}", resp.getBody());

        // when
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> webClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(unknownId))
                        .bodyValue(badRenterMissingText).retrieve().toEntity(Void.class).block(),
                         WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("renter is not valid", unknownId));
    }

    @Test
    //@Disabled
    void add_bad_renter_rejected() {
        // given
        
        RenterDTO badRenterMissingText = new RenterDTO();
        MediaType contentType = MediaType.valueOf( MediaType.APPLICATION_XML_VALUE);        
        // when
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
        
        () ->  webClient.post().uri(rentersUrl).contentType(contentType)
                                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)).bodyValue(badRenterMissingText)
                                .retrieve().toEntity(RenterDTO.class).block(), WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("renter is not valid ", ""));
    }

    public static class IntegerConverter implements ArgumentConverter {
        @Override
        public Object convert(Object o, ParameterContext parameterContext) throws ArgumentConversionException {
            return o.equals("null") ? null : Integer.parseInt((String)o);
        }
    }
    @Test
    void get_empty_renters(){
        // given - we have no quotes
        Integer pageNumber = 0;
        Integer pageSize = 100;
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH);
        if (pageNumber!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", pageSize);
        }
        URI url = urlBuilder.build().toUri();

        
         //when - asked for amounts we do not have
        ResponseEntity<RenterListDTO> response = webClient.get().uri(url).retrieve().toEntity(RenterListDTO.class).block();
        
        log.debug("{}", response);

        //then - the response will be empty
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        RenterListDTO returnedRenters = response.getBody();
        BDDAssertions.then(returnedRenters.getCount()).isEqualTo(0);
        //and descriptive attributes filed in
        BDDAssertions.then(returnedRenters.getOffset()).isEqualTo(0);
        BDDAssertions.then(returnedRenters.getLimit()).isEqualTo(100);
        BDDAssertions.then(returnedRenters.getTotal()).isEqualTo(0);       

    }

    @Test
    void get_many_renters() {
        // given many quotes
        given_many_renters(100);

        //when asking for a page of quotes
        Integer pageNumber = 9;
        Integer pageSize = 10;
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH);
        if (pageNumber!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", pageSize);
        }
        URI url = urlBuilder.build().toUri();

        ResponseEntity<RenterListDTO> response = webClient.get().uri(url).retrieve().toEntity(RenterListDTO.class).block();
        

         //then - page of results returned
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        RenterListDTO returnedRenters = response.getBody();
        log.debug("{}", returnedRenters);
        BDDAssertions.then(returnedRenters.getCount()).isEqualTo(10);
        RenterDTO renter0 = returnedRenters.getRenters().get(0);
        String[] ids = renter0.getId().split("-");
        
        BDDAssertions.then(Integer.valueOf(ids[1])).isGreaterThan(1);
        

        //and descriptive attributes filed in
        BDDAssertions.then(returnedRenters.getOffset()).isEqualTo(9);
        BDDAssertions.then(returnedRenters.getLimit()).isEqualTo(10);
        BDDAssertions.then(returnedRenters.getTotal()).isEqualTo(10);
    }


}

