package info.ejava.assignments.api.autorentals.svc.main.renter.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
import info.ejava.assignments.api.autorentals.svc.main.renter.RenterTestConfiguration;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoRentalsAppMain.class, RenterTestConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterHttpIfaceClientNTest {

    @Autowired
    private RentersAPI renterHttpIfaceAPI;

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
        //ResponseEntity<RenterDTO> resp = renterHttpIfaceAPI.createRenter(validRenter);
        //log.info("response - {}", resp);
        renterHttpIfaceAPI.removeAllRenters();
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_renter_for_type(MediaType contentType, MediaType accept){
        //log.info("port way1 - {}", port);
        // given / arrange

        // when  / act
        ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.createRenter(validRenter) ;
     
        log.info("resp. status - {} - {}", response.getStatusCode(), HttpStatus.valueOf(response.getStatusCode().value()));
        log.info("resp. body - {}", response.getBody());
        log.info("resp. header Content Type- {}", response.getHeaders().getContentType());

        // then / evaluate-assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        

        RenterDTO createdRenter = response.getBody();
        BDDAssertions.then(createdRenter).isEqualTo(validRenter.withId(createdRenter.getId()));
        URI location = UriComponentsBuilder.fromUri(baseUrl).replacePath(RentersAPI.RENTER_PATH).build(createdRenter.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());
    }

    @Test
    void get_renter() {
        // given / arrange
        RenterDTO existingRenter = renterDTOFactory.make();
        
        // when / act
        ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.createRenter(existingRenter);

        // then / assert -evaluate
        BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();

        String requestId = response.getBody().getId();
        URI location = UriComponentsBuilder.fromUri(baseUrl).replacePath(RentersAPI.RENTER_PATH).build(requestId);
        ResponseEntity<RenterDTO> getRenter = renterHttpIfaceAPI.getRenter(requestId);

        BDDAssertions.then(getRenter.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getRenter.getBody()).isEqualTo(existingRenter.withId(requestId));
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(location);
    }

     @ParameterizedTest
     @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
     void get_renters(String mediaTypeString){
        // given / arrange
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String,RenterDTO> rentersMap = new HashMap<>();
        RenterListDTO renters = renterDTOFactory.listBuilder().make(3,3);
        for (RenterDTO renterDTO : renters.getRenters()) {
            ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.createRenter(renterDTO);
            BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
            RenterDTO addedRenter = response.getBody();
            rentersMap.put(addedRenter.getId(), addedRenter);
        }
        BDDAssertions.then(rentersMap).isNotEmpty();

      
        // when / act
        ResponseEntity<RenterListDTO> response = renterHttpIfaceAPI.getRenters(0, 0);
        ResponseEntity<RenterListDTO> responseWithOffsetLimit  = renterHttpIfaceAPI.getRenters(0, 15 );

        // then / evaluate - assert

        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetLimit.getStatusCode()).isEqualTo(HttpStatus.OK);
        RenterListDTO renterPage = response.getBody();
        RenterListDTO renterPageWithOffset = responseWithOffsetLimit.getBody();
        //log.info("offset - {}, limit - {}", renterPage.getOffset(), renterPage.getLimit());
        BDDAssertions.then(renterPage.getOffset()).isEqualTo(0);
        BDDAssertions.then(renterPage.getLimit()).isEqualTo(0);
        log.info("renter map size - {}",rentersMap.size());
        log.info("count-{}, total -{}",renterPage.getCount(), renterPage.getTotal());
        
        BDDAssertions.then(renterPage.getCount()).isEqualTo(rentersMap.size());

        BDDAssertions.then(renterPageWithOffset.getOffset()).isEqualTo(0);
        BDDAssertions.then(renterPageWithOffset.getLimit()).isEqualTo(15);
        log.info("offset count-{}, total -{}",renterPageWithOffset.getCount(), renterPageWithOffset.getTotal());
        //BDDAssertions.then(renterPageWithOffset.getCount()).isEqualTo(rentersMap.size()-2);

        for(RenterDTO renter: renterPage.getRenters()){
            BDDAssertions.then(rentersMap.remove(renter.getId())).isNotNull();
        }

        BDDAssertions.then(rentersMap).isEmpty();

     }

     @ParameterizedTest
     @MethodSource("mediaTypes")
     void add_valid_renter(MediaType contentType, MediaType accept){
        // given / arrange 
        RenterDTO validRenter = renterDTOFactory.make();

        // when / act

        ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.createRenter(validRenter);

        // then / evaluate - assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        RenterDTO createdQuote = response.getBody();
        BDDAssertions.then(createdQuote).isEqualTo(validRenter.withId(createdQuote.getId()));
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTER_PATH).build(createdQuote.getId());
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(location);
     }

    private RenterDTO given_an_existing_renter() {
        RenterDTO existingRenter = renterDTOFactory.make();
        ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.createRenter(existingRenter);
        BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
        BDDAssertions.then((response.getStatusCode())).isEqualTo(HttpStatus.CREATED);
        return response.getBody();

     }

    @Test
    void update_an_existing_renter() {
        // given - an existing renter
        RenterDTO existingRenter = given_an_existing_renter();
        String requestId = existingRenter.getId();

        RenterDTO updatedRenter = existingRenter.withFirstName(existingRenter.getFirstName()+"Updated ").withId(null);

        // when / act
        ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.updateRenter(requestId, updatedRenter);

        // then / evaluate - assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<RenterDTO> getupdatedRenter = renterHttpIfaceAPI.getRenter(requestId);

        BDDAssertions.then(getupdatedRenter.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getupdatedRenter.getBody()).isEqualTo(updatedRenter.withId(requestId));
        BDDAssertions.then(getupdatedRenter.getBody()).isNotEqualTo(existingRenter);

     }

      @Test
     void get_renter_1(){
        // given / arrange
        RenterDTO existingRenter = renterDTOFactory.make();
        ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.createRenter(existingRenter);
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act
        ResponseEntity<RenterDTO>  getRenter = renterHttpIfaceAPI.getRenter(requestId);

        // then
        BDDAssertions.then(getRenter.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getRenter.getBody()).isEqualTo(existingRenter.withId(requestId));
     }

     protected List<RenterDTO> given_many_renters(int count) {
        List<RenterDTO> renters = new ArrayList<>(count);
        for (RenterDTO renterDTO : renterDTOFactory.listBuilder().renters(count, count)) {
            ResponseEntity<RenterDTO> response = renterHttpIfaceAPI.createRenter(renterDTO);
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            renters.add(response.getBody());
        }
        return renters;
     }

    @Test
     void remove_renter() {
        // given
        List<RenterDTO> renters = given_many_renters(5);
        String requestId = renters.get(1).getId();
        BDDAssertions.then(renterHttpIfaceAPI.getRenter(requestId).getStatusCode()).isEqualTo(HttpStatus.OK);

        // when requested to remove
        ResponseEntity<Void> response = renterHttpIfaceAPI.removeRenter(requestId);

        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(()-> renterHttpIfaceAPI.getRenter(requestId),
                                         RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
     }
    
    @Test
    void remove_all_renter() {
        // given / arrange
        List<RenterDTO> renters = given_many_renters(6);

        // when / act
        ResponseEntity<Void> resp = renterHttpIfaceAPI.removeAllRenters();

        // then 
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (RenterDTO renterDTO : renters) {
            RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                    () -> renterHttpIfaceAPI.getRenter(renterDTO.getId()),
                    RestClientResponseException.class);
            BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void remove_unknown_renter() {  // idempotent http method
        // given 
        String requestId = "renter-13";

        // when
        ResponseEntity<Void> resp = renterHttpIfaceAPI.removeRenter(requestId);

        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void get_renter_no_renters(){
        // given
        BDDAssertions.assertThat(renterHttpIfaceAPI.removeAllRenters().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // then
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> renterHttpIfaceAPI.getRenter("renter-123")
                                    , RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getMessage()).contains("Not Found");
        log.info("random quote -  {}", errMsg);
    }

    @Test
    void get_unknown_renter(){
        // given
        String unknownId ="renter-13";

        // when - requesting quote by id

        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> renterHttpIfaceAPI.getRenter(unknownId)       
                            , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("Renter-[%s]", unknownId));

    }

    @Test
    void update_unknown_renter() {
        // given

        String unknownId = "13";
        RenterDTO updateRenter = renterDTOFactory.make().withId(unknownId);

        // verify that updating existing quote
        RestClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()-> renterHttpIfaceAPI.updateRenter(unknownId, updateRenter)
                    , RestClientResponseException.class);

        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("renter is not valid", unknownId));
    }

    @Test
    //@Disabled
    void update_known_renter_with_bad_renter() {
        // given
        List<RenterDTO> renters = given_many_renters(3);
        
        String knownId = renters.get(0).getId();
        RenterDTO badQuoteMissingText = new RenterDTO();
        badQuoteMissingText.withId(knownId);
        ResponseEntity<RenterDTO> resp = renterHttpIfaceAPI.getRenter(knownId);
        log.info("resp - {}", resp.getBody());

        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> renterHttpIfaceAPI.updateRenter(knownId, badQuoteMissingText)
                    , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("renter is not valid", knownId));
    }

    @Test
    //@Disabled
    void add_bad_renter_rejected() {
        // given
        
        RenterDTO badRenterMissingText = new RenterDTO();
        // ResponseEntity<RenterDTO> resp =   webClient.post()
        //             .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
        //             .bodyValue(badQuoteMissingText)
        //             .retrieve().toEntity(RenterDTO.class).block();
        // log.info("resp - {} ", resp.getBody());

        
        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
        //  () -> webClient.post().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
        //             .bodyValue(badQuoteMissingText).retrieve().toEntity(RenterDTO.class).block()
        () -> renterHttpIfaceAPI.createRenter(badRenterMissingText)
                    , RestClientResponseException.class);
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
        ResponseEntity<RenterListDTO> response = renterHttpIfaceAPI.getRenters(0,100);
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

        ResponseEntity<RenterListDTO> response = renterHttpIfaceAPI.getRenters(offset, limit);

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