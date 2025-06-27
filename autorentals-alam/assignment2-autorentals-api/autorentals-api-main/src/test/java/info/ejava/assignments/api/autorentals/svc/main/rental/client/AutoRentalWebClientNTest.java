package info.ejava.assignments.api.autorentals.svc.main.rental.client;

import java.net.URI;
import java.time.LocalDate;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.rental.AutoRentalTestConfiguration;
import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsAPI;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoRentalTestConfiguration.class,AutoRentalsAppMain.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRentalWebClientNTest {
     @Autowired 
    private WebClient webClient;

    @LocalServerPort
    private int port;  // injecting port way -1

    @Autowired
    private URI baseUrl;

    @Autowired
    private AutoRentalDTOFactory autoRentalDTOFactory;

    @Autowired @Qualifier("validAutoRental")
    private AutoRentalDTO validAutoRental;

    @Autowired @Qualifier("invalidAutoRental")
    private AutoRentalDTO invalidAutoRental;

    @Autowired
    private URI autoRentalUrl;

        
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
        webClient.delete().uri(autoRentalUrl).retrieve().toEntity(Void.class).block() ;         
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_autoRental_for_type(MediaType contentType, MediaType accept){

        // given - a valid auto
      
        AutoRentalDTO validAutoRentalDTO = autoRentalDTOFactory.make();
        log.info("Content-Type-{}, Accept-Type-{}, auto -{}", contentType, accept, validAutoRentalDTO);

        // when - making a request with different content and accept payload types
        RequestEntity<AutoRentalDTO> request = RequestEntity
                                            .post(autoRentalUrl)
                                            .contentType(contentType)
                                            .accept(accept)
                                            .body(validAutoRentalDTO);

        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        
        ResponseEntity<AutoRentalDTO> response = webClient.post().uri(autoRentalUrl)
                                            .contentType(contentType).accept(accept)
                                            .bodyValue(validAutoRentalDTO).retrieve()
                                            .toEntity(AutoRentalDTO.class).block();

        log.info("resp. body - {}", response.getBody());
        log.info("resp. content type - {}", response.getHeaders().getContentType());
        
        // then - the service will accept the format we supplied
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BDDAssertions.then(response.getHeaders().getContentType()).isEqualTo(accept);
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo(accept.toString());

        // that equals what we sent and plus an ID generated
        AutoRentalDTO createdAutoRental = response.getBody();
        BDDAssertions.then(createdAutoRental).isEqualTo(validAutoRentalDTO.withId(createdAutoRental.getId()));
        // with a location reponse header referencing the URI of the created renter
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(createdAutoRental.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());
        
    }

    @Test
    void get_autoRental(){
        // given/ arrange - an existing renter
        
        
        ResponseEntity<AutoRentalDTO> response = webClient.post()
                                            .uri(autoRentalUrl).bodyValue(validAutoRental)
                                            .retrieve().toEntity(AutoRentalDTO.class).block();
        BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();        

        String requestId = response.getBody().getId();
        
        URI renterUrl = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(renterUrl);
        
        // when / act - requesting renter get by id
        ResponseEntity<AutoRentalDTO> renterResponse  = webClient.get().uri(renterUrl).retrieve().toEntity(AutoRentalDTO.class).block();
        
        // then
        BDDAssertions.then(renterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(renterResponse.getBody()).isEqualTo(validAutoRental.withId(requestId));

    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void get_autoRentals(String mediaTypeString){
        // given / arrange
        log.info("mediaTypeString - {}", mediaTypeString);
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String,AutoRentalDTO> existingAutoRentals = new HashMap<>();
        AutoRentalListDTO autoRentals = autoRentalDTOFactory.listBuilder().make(40, 40);
        for (AutoRentalDTO autoRental : autoRentals.getAutoRentals()) {
            ResponseEntity<AutoRentalDTO> response = webClient.post().uri(autoRentalUrl).contentType(mediaType)
                                                .bodyValue(autoRental).retrieve().toEntity(AutoRentalDTO.class).block();
            BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            AutoRentalDTO addedAutoRental = response.getBody();
            existingAutoRentals.put(addedAutoRental.getId(), addedAutoRental);
        }
        BDDAssertions.assertThat(existingAutoRentals).isNotEmpty();
        URI autoRentalsUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri();
        URI autoRentalsUriWithpageNumberAndpageSize = UriComponentsBuilder.fromUri(baseUrl)
                                        .path(AutoRentalsAPI.AUTO_RENTALS_PATH)
                                        .queryParam("pageNumber", 1)
                                        .queryParam("pageSize", 20)
                                        .build().toUri();

        // when / act
        
        ResponseEntity<AutoRentalListDTO> response = webClient.get().uri(autoRentalsUri)
                                                .accept(mediaType).retrieve().toEntity(AutoRentalListDTO.class).block();
        
        ResponseEntity<AutoRentalListDTO> responseWithpageNumberAndpageSize = webClient.get().uri(autoRentalsUriWithpageNumberAndpageSize)
                                                                    .accept(mediaType).retrieve().toEntity(AutoRentalListDTO.class).block();
                                            

        // then / evaluate and assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithpageNumberAndpageSize.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoRentalListDTO autoRentalPageWithoutpageNumberAndpageSize = response.getBody();
        AutoRentalListDTO autoRentalPageWithpageNumberAndpageSize = responseWithpageNumberAndpageSize.getBody();

        
        BDDAssertions.then(autoRentalPageWithoutpageNumberAndpageSize.getOffset()).isEqualTo(0);
        BDDAssertions.then(autoRentalPageWithoutpageNumberAndpageSize.getLimit()).isEqualTo(0);
        BDDAssertions.then(autoRentalPageWithpageNumberAndpageSize.getOffset()).isEqualTo(1);
        BDDAssertions.then(autoRentalPageWithpageNumberAndpageSize.getLimit()).isEqualTo(20);

        BDDAssertions.then(autoRentalPageWithoutpageNumberAndpageSize.getCount()).isEqualTo(existingAutoRentals.size());
        BDDAssertions.then(autoRentalPageWithpageNumberAndpageSize.getCount()).isEqualTo(20);
        
         for (AutoRentalDTO q: autoRentalPageWithoutpageNumberAndpageSize.getAutoRentals()) {
            BDDAssertions.then(existingAutoRentals.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(existingAutoRentals).isEmpty();

    }


    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_autoRental(MediaType contentType, MediaType accept) {
        // given / arrange - a valid auto
                                                            
        // when / act 
        ResponseEntity<AutoRentalDTO> response = webClient.post().uri(autoRentalUrl)
                                            .contentType(contentType).bodyValue(validAutoRental)
                                            .retrieve().toEntity(AutoRentalDTO.class).block();
        
        // then 
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // that equals what we sent plus an id generated
        
        AutoRentalDTO createdAutoRental = response.getBody();
        BDDAssertions.then(createdAutoRental).isEqualTo(validAutoRental.withId(createdAutoRental.getId()));
        // a lcation response header referencing the URL for the created resources
        URI location = UriComponentsBuilder.fromUri(baseUrl) .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(createdAutoRental.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).as("location error").isEqualTo(location.toString());
    }

    
    private AutoRentalDTO given_an_existing_autoRental(){
        AutoRentalDTO existingAutoRental = autoRentalDTOFactory.make();
        ResponseEntity<AutoRentalDTO> response =  webClient.post().uri(autoRentalUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .bodyValue(existingAutoRental)
                                                            .retrieve().toEntity(AutoRentalDTO.class).block();
    
        BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }


    @Test
        void update_an_existing_autoRental_whose_timePeriod_is_not_overlapped( ){
        // given - an existing auto
        AutoRentalDTO existingAutoRental = given_an_existing_autoRental();
        String requestId = existingAutoRental.getId();

        // and an update 
        log.info("startDate - {} , endDate - {}", existingAutoRental.getStartDate(), existingAutoRental.getEndDate());
        AutoRentalDTO updatedAutoRental = existingAutoRental.withMakeModel(existingAutoRental.getMakeModel()+"Updated ")
                                                .withStartDate(existingAutoRental.getStartDate().plusDays(1)).withId(null);

        log.info("startDate - {} , endDate - {}", existingAutoRental.getStartDate(), existingAutoRental.getEndDate());


        URI updateUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(existingAutoRental.getId());

        // when - updating existing quote
        ResponseEntity<Void> response = webClient.put().uri(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(updatedAutoRental)
                                                        .retrieve().toEntity(Void.class).block();
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        ResponseEntity<AutoRentalDTO> getUpdatedAutoRental = webClient.get().uri(getUri).retrieve().toEntity( AutoRentalDTO.class).block();

        BDDAssertions.then(getUpdatedAutoRental.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedAutoRental.getBody()).isEqualTo(updatedAutoRental.withId(existingAutoRental.getId()));
        BDDAssertions.then(getUpdatedAutoRental.getBody()).isNotEqualTo(existingAutoRental);

    }

    @Test
    void get_autoRental_1() {
        // given / arrange
        AutoRentalDTO existingAutoRental = autoRentalDTOFactory.make();
        ResponseEntity<AutoRentalDTO> response = webClient.post().uri(autoRentalUrl)
                                            .bodyValue(existingAutoRental).retrieve().toEntity(AutoRentalDTO.class).block();
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act

        ResponseEntity<AutoRentalDTO> getAuto =  webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                                 .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                                 .retrieve().toEntity(AutoRentalDTO.class).block();
        

        // then
        BDDAssertions.then(getAuto.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getAuto.getBody()).isEqualTo(existingAutoRental.withId(requestId));
    }

        protected List <AutoRentalDTO> given_many_autoRentals(int count) {
        List <AutoRentalDTO> autoRentals = new ArrayList<>(count);
        for(  AutoRentalDTO autoRental : autoRentalDTOFactory.listBuilder().autoRentals(count,count)) {
                ResponseEntity<AutoRentalDTO> response = webClient.post().uri(autoRentalUrl).bodyValue(autoRental)
                                                        .retrieve().toEntity(AutoRentalDTO.class).block();
                
                    BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                autoRentals.add(response.getBody());
        }
        return autoRentals;
    }

    @Test
    void remove_autoRental(){
        // given 
        List <AutoRentalDTO> autoRentals = given_many_autoRentals(5);
        String requestId = autoRentals.get(1).getId();
        BDDAssertions.assertThat(webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                          .retrieve().toEntity(AutoRentalDTO.class).block().getStatusCode()).isEqualTo(HttpStatus.OK);
            
        // when requested to remove
        ResponseEntity<Void> resp = webClient.delete().uri(UriComponentsBuilder
                                            .fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                            .retrieve().toEntity(Void.class).block();
        
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        URI autoUri = UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        log.info("requestUri - {}, requiestId -{}", autoUri.toString(), requestId);
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId)).retrieve().toEntity(AutoRentalDTO.class).block(),
                                             WebClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
        void remove_all_autoRentals() {
        // given  / arrange
        List <AutoRentalDTO> autoRentals = given_many_autoRentals(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = webClient.delete().uri(autoRentalUrl).retrieve().toEntity(Void.class).block();
                                           
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for(  AutoRentalDTO AutoRentalDTO : autoRentals) {
               WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()-> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH)
                        .build(AutoRentalDTO.getId())).retrieve().toEntity( AutoRentalDTO.class).block(),
                         WebClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


    @Test
        void remove_unknown_autoRental() {
        // given 
        String requestId = "autoRental-13";

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = webClient.delete().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                        .retrieve().toEntity(Void.class).block();
                                                
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }


    @Test
    void get_random_autoRental_no_autoRentals(){

        // given
        BDDAssertions.assertThat( webClient.delete().uri(autoRentalUrl)
                        .retrieve().toEntity(Void.class).block().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
               
        // then
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build("autoRental-1"))
                            .retrieve().toEntity(AutoRentalDTO.class).block(),
                             WebClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains("autoRental[autoRental-1] not found");
        
    }

    @Test
        void get_unknown_autoRental(){
        // given
        String unknownId =  "autoRental-13";

        // when - requesting quote by id

        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> webClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(unknownId))
                                    .retrieve().toEntity(AutoRentalDTO.class).block(),WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("autoRental[%s] not found", unknownId));

    }


    @Test
    void update_unknown_autoRental() {
        // given

     String unknownId = "autoRental-13";
     AutoRentalDTO updateAuto = autoRentalDTOFactory.make();

        // verify that updating existing quote
        WebClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()->  webClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(unknownId))
                            .bodyValue(updateAuto).retrieve().toEntity(Void.class).block(), WebClientResponseException.class);

        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("autoRental-[%s] not found", unknownId));
    }

    @Test
    //@Disabled
        void update_known_autoRental_with_bad_autoRental() {
        // given
        List <AutoRentalDTO> autoRentals = given_many_autoRentals(3);
        
         String knownId = autoRentals.get(0).getId();
        // AutoRentalDTO badAutoRentalMissingText = new AutoRentalDTO();
        AutoRentalDTO badAutoRental = autoRentalDTOFactory.make();
        badAutoRental.setStartDate(LocalDate.now().minusDays(5));
        badAutoRental.withId(knownId);
        

        // when
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> webClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(knownId))
                        .bodyValue(badAutoRental).retrieve().toEntity(Void.class).block(),
                         WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("auto rental is not valid", knownId));
    }

    @Test
    //@Disabled
        void add_bad_autoRental_rejected() {
        // given
        
        //AutoRentalDTO badAutoRentalMissingText = new AutoRentalDTO();
        AutoRentalDTO badAutoRental = autoRentalDTOFactory.make();
        badAutoRental.setStartDate(LocalDate.now().minusMonths(2));
        MediaType contentType = MediaType.valueOf( MediaType.APPLICATION_XML_VALUE);        
        // when
        WebClientResponseException ex = BDDAssertions.catchThrowableOfType(        
        () ->  webClient.post().uri(autoRentalUrl).contentType(contentType)
                                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)).bodyValue(badAutoRental)
                                .retrieve().toEntity(AutoRentalDTO.class).block(), WebClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("auto rental is not valid ", ""));
    }

    public static class IntegerConverter implements ArgumentConverter {
        @Override
        public Object convert(Object o, ParameterContext parameterContext) throws ArgumentConversionException {
            return o.equals("null") ? null : Integer.parseInt((String)o);
        }
    }
    @Test
        void get_empty_autoRentals(){
        // given - we have no quotes
        Integer pageNumber = 0;
        Integer pageSize = 100;
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH);
        if (pageNumber!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", pageSize);
        }
        URI url = urlBuilder.build().toUri();

        
         //when - asked for amounts we do not have
        ResponseEntity<AutoRentalListDTO> response = webClient.get().uri(url).retrieve().toEntity(AutoRentalListDTO.class).block();
        
        log.debug("{}", response);

        //then - the response will be empty
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoRentalListDTO returnedautoRentals = response.getBody();
        BDDAssertions.then(returnedautoRentals.getCount()).isEqualTo(0);
        //and descriptive attributes filed in
        BDDAssertions.then(returnedautoRentals.getOffset()).isEqualTo(0);
        BDDAssertions.then(returnedautoRentals.getLimit()).isEqualTo(100);
        BDDAssertions.then(returnedautoRentals.getTotal()).isEqualTo(0);       

    }
    

    @Test
    void get_many_autoRentals() {
        // given many quotes
        given_many_autoRentals(100);

        //when asking for( a page of quotes
        Integer pageNumber = 9;
        Integer pageSize = 10;
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH);
        if (pageNumber!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", pageSize);
        }
        URI url = urlBuilder.build().toUri();

        ResponseEntity<AutoRentalListDTO> response = webClient.get().uri(url).retrieve().toEntity(AutoRentalListDTO.class).block();
        

         //then - page of results returned
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoRentalListDTO returnedautoRentals = response.getBody();
        log.debug("{}", returnedautoRentals);
        BDDAssertions.then(returnedautoRentals.getCount()).isEqualTo(10);
        AutoRentalDTO auto0 = returnedautoRentals.getAutoRentals().get(0);
        String[] ids = auto0.getId().split("-");
        
        BDDAssertions.then(Integer.valueOf(ids[1])).isGreaterThan(1);
        

        //and descriptive attributes filed in
        BDDAssertions.then(returnedautoRentals.getOffset()).isEqualTo(9);
        BDDAssertions.then(returnedautoRentals.getLimit()).isEqualTo(10);
        BDDAssertions.then(returnedautoRentals.getTotal()).isEqualTo(10);
    }

 
}

