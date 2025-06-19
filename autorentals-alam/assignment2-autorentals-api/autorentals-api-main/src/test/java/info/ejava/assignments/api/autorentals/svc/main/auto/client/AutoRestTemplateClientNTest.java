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
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
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
public class AutoRestTemplateClientNTest {
    
    @Autowired //@Qualifier("restTemplateWithLogger")
    private RestTemplate restTemplate;

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
        restTemplate.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri()) ;         
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_auto_for_type(MediaType contentType, MediaType accept){

        // given - a valid auto
      
       // AutoDTO validAuto = autoDTOFactory.make();
        log.info("Content-Type-{}, Accept-Type-{}, auto -{}", contentType, accept, validAuto);

        // when - making a request with different content and accept payload types
        RequestEntity<AutoDTO> request = RequestEntity
                                            .post(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri())
                                            .contentType(contentType)
                                            .accept(accept)
                                            .body(validAuto);

        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        
        ResponseEntity<AutoDTO> response = restTemplate.exchange(request, AutoDTO.class);

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
    void get_auto(){
        // given/ arrange - an existing auto
        
        
        ResponseEntity<AutoDTO> response = restTemplate.postForEntity(
                                UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri(), 
                                validAuto, AutoDTO.class);
        BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        

        String requestId = response.getBody().getId();
        
        URI autoUrl = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId);
        
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(autoUrl);
        RequestEntity<Void> request = RequestEntity.get(autoUrl).build();

        // when / act - requesting renter get by id
        ResponseEntity<AutoDTO> autoResponse  = restTemplate.exchange(request, AutoDTO.class);

        
        // then
        BDDAssertions.then(autoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(autoResponse.getBody()).isEqualTo(validAuto.withId(requestId));

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
            RequestEntity<AutoDTO> request = RequestEntity.post(autosUrl).body(auto);
            ResponseEntity<AutoDTO> response = restTemplate.exchange(request, AutoDTO.class);
            BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            AutoDTO addedRenter = response.getBody();
            existingAutos.put(addedRenter.getId(), addedRenter);
        }
        BDDAssertions.assertThat(existingAutos).isNotEmpty();
        URI autosUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri();
        URI autosUriWithOffsetAndLimit = UriComponentsBuilder.fromUri(baseUrl)
                                        .path(AutosAPI.AUTOS_PATH)
                                        .queryParam("pageNumber", 1)
                                        .queryParam("pageSize", 20)
                                        .build().toUri();

        // when / act
        
        ResponseEntity<AutoListDTO> response = restTemplate.exchange(
                                                    RequestEntity.get(autosUri).accept(mediaType).build(), 
                                                    AutoListDTO.class) ;
        ResponseEntity<AutoListDTO> responseWithOffsetAndLimit = restTemplate.exchange(
                                                    RequestEntity.get(autosUriWithOffsetAndLimit).accept(mediaType).build()
                                                                    , AutoListDTO.class) ;

        // then / evaluate and assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetAndLimit.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO autoPageWithoutOffsetAndLimit = response.getBody();
        AutoListDTO autoPageWithOffsetAndLimit = responseWithOffsetAndLimit.getBody();

        
        BDDAssertions.then(autoPageWithoutOffsetAndLimit.getOffset()).isNull();
        BDDAssertions.then(autoPageWithoutOffsetAndLimit.getLimit()).isNull();
        BDDAssertions.then(autoPageWithOffsetAndLimit.getOffset()).isEqualTo(1);
        BDDAssertions.then(autoPageWithOffsetAndLimit.getLimit()).isEqualTo(20);

        BDDAssertions.then(autoPageWithoutOffsetAndLimit.getCount()).isEqualTo(existingAutos.size());
        BDDAssertions.then(autoPageWithOffsetAndLimit.getCount()).isEqualTo(20);
        
         for (AutoDTO q: autoPageWithoutOffsetAndLimit.getAutos()) {
            BDDAssertions.then(existingAutos.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(existingAutos).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_auto(MediaType contentType, MediaType accept) {
        // given / arrange - a valid quote
        

       RequestEntity request =  RequestEntity.post(autosUrl).accept(accept).contentType(contentType)
                                                            .body(validAuto);
                                                            
        // when / act 
        ResponseEntity<AutoDTO> response = restTemplate.exchange(request, AutoDTO.class);
                                                            
        
        // then 
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // that equals what we sent plus an id generated
        
        AutoDTO createdAuto = response.getBody();
        BDDAssertions.then(createdAuto).isEqualTo(validAuto.withId(createdAuto.getId()));
        // a lcation response header referencing the URL for the created resources
        URI location = UriComponentsBuilder.fromUri(baseUrl) .path(AutosAPI.AUTO_PATH).build(createdAuto.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).as("location error").isEqualTo(location.toString());
    }

    private AutoDTO given_an_existing_auto(){
        AutoDTO existingAuto = autoDTOFactory.make();
        ResponseEntity<AutoDTO> response =  restTemplate.exchange(RequestEntity.post(autosUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .body(existingAuto),AutoDTO.class);
        BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void update_existing_auto( ){
        // given - an existing quote
        AutoDTO existingAuto = given_an_existing_auto();
        String requestId = existingAuto.getId();

        // and an update 
        AutoDTO updatedAuto = existingAuto.withFuelType(existingAuto.getFuelType() + "Updated ");

        URI updateUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(existingAuto.getId());

        // when - updating existing quote
        ResponseEntity<Void> response = restTemplate.exchange(RequestEntity.put(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .body(updatedAuto), Void.class);
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId);
        ResponseEntity<AutoDTO> getUpdatedAuto = restTemplate.exchange(RequestEntity.get(getUri).build(),AutoDTO.class);

        BDDAssertions.then(getUpdatedAuto.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedAuto.getBody()).isEqualTo(updatedAuto);
        BDDAssertions.then(getUpdatedAuto.getBody()).isNotEqualTo(existingAuto);

    }

    @Test
    void get_auto_1() {
        // given / arrange
        AutoDTO existingAuto = autoDTOFactory.make();
        ResponseEntity<AutoDTO> response =restTemplate.exchange(
                            RequestEntity.post(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri())
                                    .body(existingAuto),AutoDTO.class);
                                    
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act

        ResponseEntity<AutoDTO> getRenter = restTemplate.exchange( 
                RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl)
                .path(AutosAPI.AUTO_PATH).build(requestId)).build(),AutoDTO.class);

        // then
        BDDAssertions.then(getRenter.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getRenter.getBody()).isEqualTo(existingAuto.withId(requestId));
    }

    protected List<AutoDTO> given_many_autos(int count) {
        List<AutoDTO> autos = new ArrayList<>(count);
        for (AutoDTO autoDTO : autoDTOFactory.listBuilder().autos(count,count)) {
                ResponseEntity<AutoDTO> response = restTemplate.exchange(RequestEntity
                                    .post(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri())
                                    .body(autoDTO),AutoDTO.class);

                BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                autos.add(response.getBody());
        }
        return autos;
    }

    @Test
    void remove_auto(){
        // given 
        List<AutoDTO> autos = given_many_autos(5);
        String requestId = autos.get(1).getId();
        BDDAssertions.assertThat(restTemplate.exchange(
            RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId)).build()
                                    ,AutoDTO.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        // when requested to remove
        ResponseEntity<Void> resp = restTemplate.exchange(
                    RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId)).build()
                                    ,Void.class);
        // then

        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        URI autoUri = UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutosAPI.AUTO_PATH).build(requestId);
        log.info("requestUri - {}, requiestId -{}", autoUri.toString(), requestId);
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () ->restTemplate.exchange(
                                             RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutosAPI.AUTO_PATH).build(requestId)).build(),AutoDTO.class) ,
                                             RestClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void remove_all_autos() {
        // given  / arrange
        List<AutoDTO> autos = given_many_autos(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = restTemplate.exchange(
                        RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri()).build()
                                    ,Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (AutoDTO autoDTO : autos) {
               RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()->restTemplate.exchange(RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH)
                        .build(autoDTO.getId())).build(),AutoDTO.class),
                         RestClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


    @Test
    void remove_unknown_auto() {
        // given 
        String requestId = "13";

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = restTemplate.exchange(
                        RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(requestId)).build()
                                  ,Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }


    @Test
    void get_random_auto_no_autos(){
        // given
        BDDAssertions.assertThat(restTemplate.exchange(
            RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH)
                        .build().toUri()).build(),Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // then
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> restTemplate.exchange(
            RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build("auto-1")).build()
                        ,AutoDTO.class)
                                    , RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains("auto[auto-1] not found");
        
    }

    @Test
    void get_unknown_auto(){
        // given
        String unknownId =  "auto-13";

        // when - requesting auto by id

        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () ->restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(unknownId)).build()
                        ,AutoDTO.class)        
                            , RestClientResponseException.class);
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
        RestClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()-> restTemplate.exchange(
                RequestEntity.put(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(unknownId))
                    .body(updateAuto),Void.class)
            , RestClientResponseException.class);

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
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> restTemplate.exchange(
                RequestEntity.put(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(unknownId))
                    .body(badAutoMissingText),Void.class)
                    , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("auto is not valid", unknownId));
    }

    @Test
    //@Disabled
    void add_bad_auto_rejected() {
        // given
        
        AutoDTO badAutoMissingText = new AutoDTO();
                
        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
        
        () -> restTemplate.postForEntity(autosUrl, badAutoMissingText, AutoDTO.class)
                    , RestClientResponseException.class);
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
        Integer offset = 0;
        Integer limit = 100;
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", limit);
        }
        URI url = urlBuilder.build().toUri();

        
         //when - asked for amounts we do not have
        ResponseEntity<AutoListDTO> response = restTemplate.getForEntity(url, AutoListDTO.class);
        log.debug("{}", response);

        //then - the response will be empty
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO returnedRenters = response.getBody();
        BDDAssertions.then(returnedRenters.getCount()).isEqualTo(0);
        //and descriptive attributes filed in
        BDDAssertions.then(returnedRenters.getOffset()).isEqualTo(0);
        BDDAssertions.then(returnedRenters.getLimit()).isEqualTo(100);
        BDDAssertions.then(returnedRenters.getTotal()).isEqualTo(0);       

    }

    @Test
    void get_many_autos() {
        // given many quotes
        given_many_autos(100);

        //when asking for a page of quotes
        Integer offset = 9;
        Integer limit = 10;
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", limit);
        }
        URI url = urlBuilder.build().toUri();

        ResponseEntity<AutoListDTO> response = restTemplate.getForEntity(url, AutoListDTO.class);

         //then - page of results returned
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO returnedRenters = response.getBody();
        log.debug("{}", returnedRenters);
        BDDAssertions.then(returnedRenters.getCount()).isEqualTo(10);
        AutoDTO auto0 = returnedRenters.getAutos().get(0);
        String[] ids = auto0.getId().split("-");
        
        BDDAssertions.then(Integer.valueOf(ids[1])).isGreaterThan(1);
        

        //and descriptive attributes filed in
        BDDAssertions.then(returnedRenters.getOffset()).isEqualTo(9);
        BDDAssertions.then(returnedRenters.getLimit()).isEqualTo(10);
        BDDAssertions.then(returnedRenters.getTotal()).isEqualTo(10);
    }


}
