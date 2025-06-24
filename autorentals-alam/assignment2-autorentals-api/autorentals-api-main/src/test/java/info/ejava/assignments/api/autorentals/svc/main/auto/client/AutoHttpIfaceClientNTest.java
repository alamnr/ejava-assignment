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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosJSONHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoTestConfiguration.class, AutoRentalsAppMain.class},
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoHttpIfaceClientNTest {

    @Autowired @Qualifier("autosHttpIfaceJson")
    private AutosJSONHttpIfaceMapping autoHttpIfaceJsonAPI;

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
        
        autoHttpIfaceJsonAPI.removeAllAutos();
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_auto_for_type(MediaType contentType, MediaType accept){
        // given / arrange

        // when  / act
        ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.createAuto(validAuto) ;
     
        log.info("resp. status - {} - {}", response.getStatusCode(), HttpStatus.valueOf(response.getStatusCode().value()));
        log.info("resp. body - {}", response.getBody());
        log.info("resp. header Content Type- {}", response.getHeaders().getContentType());

        // then / evaluate-assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        

        AutoDTO createdRenter = response.getBody();
        BDDAssertions.then(createdRenter).isEqualTo(validAuto.withId(createdRenter.getId()));
        URI location = UriComponentsBuilder.fromUri(baseUrl).replacePath(AutosAPI.AUTO_PATH).build(createdRenter.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());
    }

    @Test
    void get_auto() {
        // given / arrange
        AutoDTO existingAuto = autoDTOFactory.make();
        
        // when / act
        ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.createAuto(existingAuto);

        // then / assert -evaluate
        BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();

        String requestId = response.getBody().getId();
        URI location = UriComponentsBuilder.fromUri(baseUrl).replacePath(AutosAPI.AUTO_PATH).build(requestId);
        ResponseEntity<AutoDTO> getAuto = autoHttpIfaceJsonAPI.getAuto(requestId);

        BDDAssertions.then(getAuto.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getAuto.getBody()).isEqualTo(existingAuto.withId(requestId));
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(location);
    }

     @ParameterizedTest
     @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
     void get_autos(String mediaTypeString){
        // given / arrange
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String,AutoDTO> autosMap = new HashMap<>();
        AutoListDTO autos = autoDTOFactory.listBuilder().make(3,3);
        for (AutoDTO autoDTO : autos.getAutos()) {
            ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.createAuto(autoDTO);
            BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
            AutoDTO addedRenter = response.getBody();
            autosMap.put(addedRenter.getId(), addedRenter);
        }
        BDDAssertions.then(autosMap).isNotEmpty();

      
        // when / act        
        ResponseEntity<AutoListDTO> response = autoHttpIfaceJsonAPI.searchAutosList(null,null,
                null,null,0,0);
                 
        ResponseEntity<AutoListDTO> responseWithOffsetLimit  = autoHttpIfaceJsonAPI.searchAutosList(null,null,
                null,null,0,15);

        // then / evaluate - assert

        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetLimit.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO autoPage = response.getBody();
        AutoListDTO autoPageWithOffset = responseWithOffsetLimit.getBody();
        //log.info("offset - {}, limit - {}", autoPage.getOffset(), autoPage.getLimit());
        BDDAssertions.then(autoPage.getOffset()).isEqualTo(0);
        BDDAssertions.then(autoPage.getLimit()).isEqualTo(0);
        log.info("auto map size - {}",autosMap.size());
        log.info("count-{}, total -{}",autoPage.getCount(), autoPage.getTotal());
        
        BDDAssertions.then(autoPage.getCount()).isEqualTo(autosMap.size());

        BDDAssertions.then(autoPageWithOffset.getOffset()).isEqualTo(0);
        BDDAssertions.then(autoPageWithOffset.getLimit()).isEqualTo(15);
        log.info("offset count-{}, total -{}",autoPageWithOffset.getCount(), autoPageWithOffset.getTotal());
        //BDDAssertions.then(autoPageWithOffset.getCount()).isEqualTo(autosMap.size()-2);

        for(AutoDTO auto: autoPage.getAutos()){
            BDDAssertions.then(autosMap.remove(auto.getId())).isNotNull();
        }

        BDDAssertions.then(autosMap).isEmpty();

     }

     @ParameterizedTest
     @MethodSource("mediaTypes")
     void add_valid_auto(MediaType contentType, MediaType accept){
        // given / arrange 
        AutoDTO validAuto = autoDTOFactory.make();

        // when / act

        ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.createAuto(validAuto);

        // then / evaluate - assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        AutoDTO createdAuto = response.getBody();
        BDDAssertions.then(createdAuto).isEqualTo(validAuto.withId(createdAuto.getId()));
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTO_PATH).build(createdAuto.getId());
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(location);
     }

    private AutoDTO given_an_existing_auto() {
        AutoDTO existingAuto = autoDTOFactory.make();
        ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.createAuto(existingAuto);
        BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
        BDDAssertions.then((response.getStatusCode())).isEqualTo(HttpStatus.CREATED);
        return response.getBody();

     }

    @Test
    void update_an_existing_auto() {
        // given - an existing auto
        AutoDTO existingAuto = given_an_existing_auto();
        String requestId = existingAuto.getId();

        AutoDTO updatedAuto = existingAuto.withModel(existingAuto.getModel()+"Updated ").withId(null);

        // when / act
        ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.updateAuto(requestId, updatedAuto);

        // then / evaluate - assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<AutoDTO> getupdatedAuto = autoHttpIfaceJsonAPI.getAuto(requestId);

        BDDAssertions.then(getupdatedAuto.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getupdatedAuto.getBody()).isEqualTo(updatedAuto.withId(requestId));
        BDDAssertions.then(getupdatedAuto.getBody()).isNotEqualTo(existingAuto);

     }

      @Test
     void get_auto_1(){
        // given / arrange
        AutoDTO existingAuto = autoDTOFactory.make();
        ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.createAuto(existingAuto);
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act
        ResponseEntity<AutoDTO>  getAuto = autoHttpIfaceJsonAPI.getAuto(requestId);

        // then
        BDDAssertions.then(getAuto.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getAuto.getBody()).isEqualTo(existingAuto.withId(requestId));
     }

     protected List<AutoDTO> given_many_autos(int count) {
        List<AutoDTO> autos = new ArrayList<>(count);
        for (AutoDTO autoDTO : autoDTOFactory.listBuilder().autos(count, count)) {
            ResponseEntity<AutoDTO> response = autoHttpIfaceJsonAPI.createAuto(autoDTO);
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            autos.add(response.getBody());
        }
        return autos;
     }

    @Test
     void remove_auto() {
        // given
        List<AutoDTO> autos = given_many_autos(5);
        String requestId = autos.get(1).getId();
        BDDAssertions.then(autoHttpIfaceJsonAPI.getAuto(requestId).getStatusCode()).isEqualTo(HttpStatus.OK);

        // when requested to remove
        ResponseEntity<Void> response = autoHttpIfaceJsonAPI.removeAuto(requestId);

        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(()-> autoHttpIfaceJsonAPI.getAuto(requestId),
                                         RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
     }
    
    @Test
    void remove_all_auto() {
        // given / arrange
        List<AutoDTO> autos = given_many_autos(6);

        // when / act
        ResponseEntity<Void> resp = autoHttpIfaceJsonAPI.removeAllAutos();

        // then 
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (AutoDTO autoDTO : autos) {
            RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                    () -> autoHttpIfaceJsonAPI.getAuto(autoDTO.getId()),
                    RestClientResponseException.class);
            BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void remove_unknown_auto() {  // idempotent http method
        // given 
        String requestId = "auto-13";

        // when
        ResponseEntity<Void> resp = autoHttpIfaceJsonAPI.removeAuto(requestId);

        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void get_auto_no_autos(){
        // given
        BDDAssertions.assertThat(autoHttpIfaceJsonAPI.removeAllAutos().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // then
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> autoHttpIfaceJsonAPI.getAuto("auto-123")
                                    , RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getMessage()).contains("Not Found");
        log.info("random quote -  {}", errMsg);
    }

    @Test
    void get_unknown_auto(){
        // given
        String unknownId ="auto-13";

        // when - requesting quote by id

        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> autoHttpIfaceJsonAPI.getAuto(unknownId)       
                            , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("auto[%s]", unknownId));

    }

    @Test
    void update_unknown_auto() {
        // given

        String unknownId = "13";
        AutoDTO updateAuto = autoDTOFactory.make().withId(unknownId);

        // verify that updating existing quote
        RestClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()-> autoHttpIfaceJsonAPI.updateAuto(unknownId, updateAuto)
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
        
        String knownId = autos.get(0).getId();
        AutoDTO badAutoMissingText = new AutoDTO();
        badAutoMissingText.withId(knownId);
        ResponseEntity<AutoDTO> resp = autoHttpIfaceJsonAPI.getAuto(knownId);
        log.info("resp - {}", resp.getBody());

        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> autoHttpIfaceJsonAPI.updateAuto(knownId, badAutoMissingText)
                    , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("auto is not valid", knownId));
    }

    @Test
    //@Disabled
    void add_bad_auto_rejected() {
        // given
        
        AutoDTO badAutoMissingText = new AutoDTO();
        
        
        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
        //  () -> webClient.post().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
        //             .bodyValue(badQuoteMissingText).retrieve().toEntity(AutoDTO.class).block()
        () -> autoHttpIfaceJsonAPI.createAuto(badAutoMissingText)
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
        // UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH);
        // if (offset!=null) {
        //     urlBuilder = urlBuilder.queryParam("offset", offset);
        // }
        // if (limit!=null) {
        //     urlBuilder = urlBuilder.queryParam("limit", limit);
        // }
        // URI url = urlBuilder.build().toUri();

        
         //when - asked for amounts we do not have
        ResponseEntity<AutoListDTO> response = autoHttpIfaceJsonAPI.searchAutosList(null,null,
                null,null,0,100);
        log.debug("{}", response);

        //then - the response will be empty
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO returnedAutos = response.getBody();
        BDDAssertions.then(returnedAutos.getCount()).isEqualTo(0);
        //and descriptive attributes filed in
        log.info("returned - {}", returnedAutos);
        BDDAssertions.then(returnedAutos.getOffset()).isEqualTo(0);
        BDDAssertions.then(returnedAutos.getLimit()).isEqualTo(100);
        BDDAssertions.then(returnedAutos.getTotal()).isEqualTo(0);       

    }

    @Test
    void get_many_autos() {
        // given many quotes
        given_many_autos(100);

        //when asking for a page of quotes
        Integer offset = 9;
        Integer limit = 10;
        //  UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH);
        // if (offset!=null) {
        //     urlBuilder = urlBuilder.queryParam("offset", offset);
        // }
        // if (limit!=null) {
        //     urlBuilder = urlBuilder.queryParam("limit", limit);
        // }
        // URI url = urlBuilder.build().toUri();
        ResponseEntity<AutoListDTO> response = autoHttpIfaceJsonAPI.searchAutosList(null,null,
                null,null,offset,limit);

         //then - page of results returned
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoListDTO returnedAutos = response.getBody();
        log.debug("{}", returnedAutos);
        BDDAssertions.then(returnedAutos.getCount()).isEqualTo(10);
        AutoDTO auto0 = returnedAutos.getAutos().get(0);
        String[] ids = auto0.getId().split("-");
        
        BDDAssertions.then(Integer.valueOf(ids[1])).isGreaterThan(1);
        

        //and descriptive attributes filed in
        BDDAssertions.then(returnedAutos.getOffset()).isEqualTo(9);
        BDDAssertions.then(returnedAutos.getLimit()).isEqualTo(10);
        BDDAssertions.then(returnedAutos.getTotal()).isEqualTo(10);
    }

   
}