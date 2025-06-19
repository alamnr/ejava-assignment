package info.ejava.assignments.api.autorentals.svc.main.auto.client;

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
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoTestConfiguration.class,AutoRentalsAppMain.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoWebClientNTest {
     @Autowired 
    private WebClient webClient;

    @LocalServerPort
    private int port;  // injecting port way -1

    @Autowired
    private URI baseUrl;

    @Autowired
    private AutoDTOFactory autoDTOFactory;

    @Autowired @Qualifier("validAuto")
    private AutoDTO validAuto;

    @Autowired @Qualifier("invalidAuto")
    private AutoDTO invalidAuto;

    @Autowired
    private URI autosUrl;

        
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
        webClient.delete().uri(autosUrl).retrieve().toEntity(Void.class).block() ;         
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_auto_for_type(MediaType contentType, MediaType accept){

        // given - a valid auto
      
       // AutoDTO validAuto = autoDTOFactory.make();
        log.info("Content-Type-{}, Accept-Type-{}, auto -{}", contentType, accept, validAuto);

        // when - making a request with different content and accept payload types
        RequestEntity<AutoDTO> request = RequestEntity
                                            .post(autosUrl)
                                            .contentType(contentType)
                                            .accept(accept)
                                            .body(validAuto);

        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        
        ResponseEntity<AutoDTO> response = webClient.post().uri(autosUrl)
                                            .contentType(contentType).accept(accept)
                                            .bodyValue(validAuto).retrieve()
                                            .toEntity(AutoDTO.class).block();

        log.info("resp. body - {}", response.getBody());
        log.info("resp. content type - {}", response.getHeaders().getContentType());
        
        // then - the service will accept the format we supplied
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BDDAssertions.then(response.getHeaders().getContentType()).isEqualTo(accept);
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo(accept.toString());

        // that equals what we sent and plus an ID generated
        AutoDTO createdAuto = response.getBody();
        BDDAssertions.then(createdAuto).isEqualTo(validAuto.withId(createdAuto.getId()));
        // with a location reponse header referencing the URI of the created renter
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(createdAuto.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());
        
    }

    @Test
    void get_renter(){
        // given/ arrange - an existing renter
        
        
        ResponseEntity<AutoDTO> response = webClient.post()
                                            .uri(autosUrl).bodyValue(validAuto)
                                            .retrieve().toEntity(AutoDTO.class).block();
        BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();        

        String requestId = response.getBody().getId();
        
        URI renterUrl = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId);
        
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(renterUrl);
        
        // when / act - requesting renter get by id
        ResponseEntity<AutoDTO> renterResponse  = webClient.get().uri(renterUrl).retrieve().toEntity(AutoDTO.class).block();
        
        // then
        BDDAssertions.then(renterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(renterResponse.getBody()).isEqualTo(validAuto.withId(requestId));

    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void get_autos(String mediaTypeString){
        // given / arrange
        log.info("mediaTypeString - {}", mediaTypeString);
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String,AutoDTO> existingAutos = new HashMap<>();
        AutoListDTO autos = autoDTOFactory.listBuilder().make(40, 40);
        for (AutoDTO auto : autos.getAutos()) {
            ResponseEntity<AutoDTO> response = webClient.post().uri(autosUrl).contentType(mediaType)
                                                .bodyValue(auto).retrieve().toEntity(AutoDTO.class).block();
            BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            AutoDTO addedRenter = response.getBody();
            existingAutos.put(addedRenter.getId(), addedRenter);
        }
        BDDAssertions.assertThat(existingAutos).isNotEmpty();
        URI autosUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri();
        URI autosUriWithpageNumberAndpageSize = UriComponentsBuilder.fromUri(baseUrl)
                                        .path(AutosAPI.AUTOS_PATH)
                                        .queryParam("pageNumber", 1)
                                        .queryParam("pageSize", 20)
                                        .build().toUri();

        // when / act
        
        ResponseEntity<AutoListDTO> response = webClient.get().uri(autosUri)
                                                .accept(mediaType).retrieve().toEntity(AutoListDTO.class).block();
        
        ResponseEntity<AutoListDTO> responseWithpageNumberAndpageSize = webClient.get().uri(autosUriWithpageNumberAndpageSize)
                                                                    .accept(mediaType).retrieve().toEntity(AutoListDTO.class).block();
                                            

        // then / evaluate and assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithpageNumberAndpageSize.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO autoPageWithoutpageNumberAndpageSize = response.getBody();
        AutoListDTO autoPageWithpageNumberAndpageSize = responseWithpageNumberAndpageSize.getBody();

        
        BDDAssertions.then(autoPageWithoutpageNumberAndpageSize.getOffset()).isNull();
        BDDAssertions.then(autoPageWithoutpageNumberAndpageSize.getLimit()).isNull();
        BDDAssertions.then(autoPageWithpageNumberAndpageSize.getOffset()).isEqualTo(1);
        BDDAssertions.then(autoPageWithpageNumberAndpageSize.getLimit()).isEqualTo(20);

        BDDAssertions.then(autoPageWithoutpageNumberAndpageSize.getCount()).isEqualTo(existingAutos.size());
        BDDAssertions.then(autoPageWithpageNumberAndpageSize.getCount()).isEqualTo(20);
        
         for (AutoDTO q: autoPageWithoutpageNumberAndpageSize.getAutos()) {
            BDDAssertions.then(existingAutos.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(existingAutos).isEmpty();

    }

    
    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_auto(MediaType contentType, MediaType accept) {
        // given / arrange - a valid auto
                                                            
        // when / act 
        ResponseEntity<AutoDTO> response = webClient.post().uri(autosUrl)
                                            .contentType(contentType).bodyValue(validAuto)
                                            .retrieve().toEntity(AutoDTO.class).block();
        
        // then 
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // that equals what we sent plus an id generated
        
        AutoDTO createdAuto = response.getBody();
        BDDAssertions.then(createdAuto).isEqualTo(validAuto.withId(createdAuto.getId()));
        // a lcation response header referencing the URL for the created resources
        URI location = UriComponentsBuilder.fromUri(baseUrl) .path(AutosAPI.AUTO_PATH).build(createdAuto.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).as("location error").isEqualTo(location.toString());
    }

    private AutoDTO given_an_existing_renter(){
        AutoDTO existingAuto = autoDTOFactory.make();
        ResponseEntity<AutoDTO> response =  webClient.post().uri(autosUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .bodyValue(existingAuto)
                                                            .retrieve().toEntity(AutoDTO.class).block();
        
        BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void update_existing_quote( ){
        // given - an existing quote
        AutoDTO existingAuto = given_an_existing_renter();
        String requestId = existingAuto.getId();

        // and an update 
        AutoDTO updatedAuto = existingAuto.withModel(existingAuto.getModel() + "Updated ");

        URI updateUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(existingAuto.getId());

        // when - updating existing quote
        ResponseEntity<Void> response = webClient.put().uri(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(updatedAuto)
                                                        .retrieve().toEntity(Void.class).block();
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId);
        ResponseEntity<AutoDTO> getUpdatedAuto = webClient.get().uri(getUri).retrieve().toEntity(AutoDTO.class).block();

        BDDAssertions.then(getUpdatedAuto.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedAuto.getBody()).isEqualTo(updatedAuto);
        BDDAssertions.then(getUpdatedAuto.getBody()).isNotEqualTo(existingAuto);

    }

    @Test
    void get_auto_1() {
        // given / arrange
        AutoDTO existingAuto = autoDTOFactory.make();
        ResponseEntity<AutoDTO> response = webClient.post().uri(autosUrl)
                                            .bodyValue(existingAuto).retrieve().toEntity(AutoDTO.class).block();
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act

        ResponseEntity<AutoDTO> getAuto =  webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                                 .path(AutosAPI.AUTO_PATH).build(requestId))
                                                 .retrieve().toEntity(AutoDTO.class).block();
        

        // then
        BDDAssertions.then(getAuto.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getAuto.getBody()).isEqualTo(existingAuto.withId(requestId));
    }

    protected List<AutoDTO> given_many_autos(int count) {
        List<AutoDTO> autos = new ArrayList<>(count);
        for (AutoDTO AutoDTO : autoDTOFactory.listBuilder().autos(count,count)) {
                ResponseEntity<AutoDTO> response = webClient.post().uri(autosUrl).bodyValue(AutoDTO).retrieve().toEntity(AutoDTO.class).block();
                
                BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                autos.add(response.getBody());
        }
        return autos;
    }

    @Test
    void remove_auto(){
        // given 
        List<AutoDTO> renters = given_many_autos(5);
        String requestId = renters.get(1).getId();
        BDDAssertions.assertThat(webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId))
                                          .retrieve().toEntity(AutoDTO.class).block().getStatusCode()).isEqualTo(HttpStatus.OK);
            
        // when requested to remove
        ResponseEntity<Void> resp = webClient.delete().uri(UriComponentsBuilder
                                            .fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId))
                                            .retrieve().toEntity(Void.class).block();
        
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        URI autoUri = UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutosAPI.AUTO_PATH).build(requestId);
        log.info("requestUri - {}, requiestId -{}", autoUri.toString(), requestId);
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutosAPI.AUTO_PATH).build(requestId)).retrieve().toEntity(AutoDTO.class).block(),
                                             WebClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void remove_all_autos() {
        // given  / arrange
        List<AutoDTO> renters = given_many_autos(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = webClient.delete().uri(autosUrl).retrieve().toEntity(Void.class).block();
                                           
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (AutoDTO AutoDTO : renters) {
               WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()-> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH)
                        .build(AutoDTO.getId())).retrieve().toEntity(AutoDTO.class).block(),
                         WebClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


    @Test
    void remove_unknown_auto() {
        // given 
        String requestId = "auto-13";

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = webClient.delete().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId))
                                        .retrieve().toEntity(Void.class).block();
                                                
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }


    @Test
    void get_random_auto_no_autos(){
        // given
        BDDAssertions.assertThat( webClient.delete().uri(autosUrl)
                        .retrieve().toEntity(Void.class).block().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
               
        // then
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build("auto-1"))
                            .retrieve().toEntity(AutoDTO.class).block(),
                             WebClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains("auto[auto-1] not found");
        
    }

    @Test
    void get_unknown_auto(){
        // given
        String unknownId =  "auto-13";

        // when - requesting quote by id

        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(unknownId))
                                    .retrieve().toEntity(AutoDTO.class).block(),WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("auto[%s] not found", unknownId));

    }


    @Test
    void update_unknown_auto() {
        // given

        String unknownId = "auto-13";
        AutoDTO updateAuto = autoDTOFactory.make();

        // verify that updating existing quote
        WebClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()->  webClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(unknownId))
                            .bodyValue(updateAuto).retrieve().toEntity(Void.class).block(), WebClientResponseException.class);

        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("auto-[%s] not found", unknownId));
    }

    @Test
    //@Disabled
    void update_known_auto_with_bad_auto() {
        // given
        List<AutoDTO> autos = given_many_autos(3);
        
        String unknownId = "auto-000";
        AutoDTO badAutoMissingText = new AutoDTO();
        badAutoMissingText.withId(unknownId);

        

        // when
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> webClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(unknownId))
                        .bodyValue(badAutoMissingText).retrieve().toEntity(Void.class).block(),
                         WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("auto is not valid", unknownId));
    }

    @Test
    //@Disabled
    void add_bad_auto_rejected() {
        // given
        
        AutoDTO badAutoMissingText = new AutoDTO();
        MediaType contentType = MediaType.valueOf( MediaType.APPLICATION_XML_VALUE);        
        // when
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(        
        () ->  webClient.post().uri(autosUrl).contentType(contentType)
                                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)).bodyValue(badAutoMissingText)
                                .retrieve().toEntity(AutoDTO.class).block(), WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("auto is not valid ", ""));
    }

    public static class IntegerConverter implements ArgumentConverter {
        @Override
        public Object convert(Object o, ParameterContext parameterContext) throws ArgumentConversionException {
            return o.equals("null") ? null : Integer.parseInt((String)o);
        }
    }
    @Test
    void get_empty_autos(){
        // given - we have no quotes
        Integer pageNumber = 0;
        Integer pageSize = 100;
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH);
        if (pageNumber!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", pageSize);
        }
        URI url = urlBuilder.build().toUri();

        
         //when - asked for amounts we do not have
        ResponseEntity<AutoListDTO> response = webClient.get().uri(url).retrieve().toEntity(AutoListDTO.class).block();
        
        log.debug("{}", response);

        //then - the response will be empty
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO returnedAutos = response.getBody();
        BDDAssertions.then(returnedAutos.getCount()).isEqualTo(0);
        //and descriptive attributes filed in
        BDDAssertions.then(returnedAutos.getOffset()).isEqualTo(0);
        BDDAssertions.then(returnedAutos.getLimit()).isEqualTo(100);
        BDDAssertions.then(returnedAutos.getTotal()).isEqualTo(0);       

    }

    @Test
    void get_many_autos() {
        // given many quotes
        given_many_autos(100);

        //when asking for a page of quotes
        Integer pageNumber = 9;
        Integer pageSize = 10;
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH);
        if (pageNumber!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", pageSize);
        }
        URI url = urlBuilder.build().toUri();

        ResponseEntity<AutoListDTO> response = webClient.get().uri(url).retrieve().toEntity(AutoListDTO.class).block();
        

         //then - page of results returned
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO returnedAutos = response.getBody();
        log.debug("{}", returnedAutos);
        BDDAssertions.then(returnedAutos.getCount()).isEqualTo(10);
        AutoDTO renter0 = returnedAutos.getAutos().get(0);
        String[] ids = renter0.getId().split("-");
        
        BDDAssertions.then(Integer.valueOf(ids[1])).isGreaterThan(1);
        

        //and descriptive attributes filed in
        BDDAssertions.then(returnedAutos.getOffset()).isEqualTo(9);
        BDDAssertions.then(returnedAutos.getLimit()).isEqualTo(10);
        BDDAssertions.then(returnedAutos.getTotal()).isEqualTo(10);
    }


}

