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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.AutoRentalsAppMain;
import info.ejava.assignments.api.autorentals.svc.main.rental.AutoRentalTestConfiguration;
import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {AutoRentalTestConfiguration.class,AutoRentalsAppMain.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRentalRestTemplateClientNTest {
    
    @Autowired //@Qualifier("restTemplateWithLogger")
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;  // injecting port way -1

    @Autowired
    private URI baseUrl;

    @Autowired
    private AutoRentalDTOFactory autoRentalDTOFactory;

    @Autowired @Qualifier("validAutoRental")
    private AutoRentalDTO validAutoRental;

    @Autowired @Qualifier("invalidAutoRental")
    private AutoRentalDTO invalidAuto;

    @Autowired
    private URI autoRentalUrl;

     @Autowired
    private AutoDTO validAuto;

    @Autowired 
    private RenterDTO validRenter;


        
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
        
        // remove all auto, renter and autoRental
        ResponseEntity<Void> responseAutoDelete = restTemplate
        .exchange(RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri()).build()
                                                    ,Void.class);
        ResponseEntity<Void> responseRenterDelete = restTemplate
        .exchange(RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH).build().toUri()).build()
                                                    ,Void.class);                            
        ResponseEntity<Void> responseAutoRentalDelete = restTemplate
        .exchange(RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri()).build()
                                    ,Void.class);
        BDDAssertions.then(responseAutoDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        BDDAssertions.then(responseRenterDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        BDDAssertions.then(responseAutoRentalDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);



        // insert a valid auto having id - "auto-1" and renter having id - "renter-1" 
        URI autoUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri();
        ResponseEntity<AutoDTO> responseAuto = restTemplate
                                                .exchange(RequestEntity.post(autoUri)
                                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                                .body(validAuto)
                                                ,AutoDTO.class);
        BDDAssertions.then(responseAuto.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        log.info("created Auto - {}", responseAuto.getBody());
        validAuto = responseAuto.getBody();

        URI renterUri = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH).build().toUri();
        ResponseEntity<RenterDTO> responseRenter = restTemplate
                                                    .exchange(RequestEntity.post(renterUri)
                                                    .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                                    .body(validRenter)
                                                    ,RenterDTO.class);
        BDDAssertions.then(responseRenter.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        log.info("created Renter - {}", responseRenter.getBody());
        validRenter = responseRenter.getBody();      
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_autoRental_for_type(MediaType contentType, MediaType accept){

        // given - a valid auto
      
        AutoRentalDTO validAutoRentalDTO = autoRentalDTOFactory.make(validAuto,validRenter,1);
        log.info("Content-Type-{}, Accept-Type-{}, autoRental -{}", contentType, accept, validAutoRentalDTO);

        // when - making a request with different content and accept payload types
        RequestEntity<AutoRentalDTO> request = RequestEntity
                                            .post(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri())
                                            .contentType(contentType)
                                            .accept(accept)
                                            .body(validAutoRentalDTO);

        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        
        ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(request, AutoRentalDTO.class);

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
        // given/ arrange - an existing autoRental
        final TimePeriod timePeriod = new TimePeriod(validAutoRental.getStartDate(), 
                                validAutoRental.getEndDate() != null ? validAutoRental.getEndDate() : validAutoRental.getStartDate());
        final AutoRentalDTO existingAutoRental = new AutoRentalDTO(validAuto, validRenter, timePeriod) ;
        
        ResponseEntity<AutoRentalDTO> response = restTemplate.postForEntity(
                                UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri(), 
                                existingAutoRental, AutoRentalDTO.class);
        BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        

        String requestId = response.getBody().getId();
        
        URI autoUrl = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(autoUrl);
        RequestEntity<Void> request = RequestEntity.get(autoUrl).build();

        // when / act - requesting renter get by id
        ResponseEntity<AutoRentalDTO> autoResponse  = restTemplate.exchange(request, AutoRentalDTO.class);

        
        // then
        BDDAssertions.then(autoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(autoResponse.getBody()).isEqualTo(existingAutoRental.withId(requestId));

    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void get_autoRentals(String mediaTypeString){
        // given / arrange
        log.info("mediaTypeString - {}", mediaTypeString);
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String,AutoRentalDTO> existingAutoRental = new HashMap<>();
        AutoRentalListDTO autoRentals = autoRentalDTOFactory.listBuilder().make(40, 40,validAuto,validRenter);
        for (AutoRentalDTO autoRental : autoRentals.getAutoRentals()) {
            RequestEntity<AutoRentalDTO> request = RequestEntity.post(autoRentalUrl).body(autoRental);
            ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(request, AutoRentalDTO.class);
            BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            AutoRentalDTO addedRental = response.getBody();
            existingAutoRental.put(addedRental.getId(), addedRental);
        }
        BDDAssertions.assertThat(existingAutoRental).isNotEmpty();
        URI rentalsUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri();
        URI rentalsUriWithOffsetAndLimit = UriComponentsBuilder.fromUri(baseUrl)
                                        .path(AutoRentalsAPI.AUTO_RENTALS_PATH)
                                        .queryParam("pageNumber", 1)
                                        .queryParam("pageSize", 20)
                                        .build().toUri();

        // when / act
        
        ResponseEntity<AutoRentalListDTO> response = restTemplate.exchange(
                                                    RequestEntity.get(rentalsUri).accept(mediaType).build(), 
                                                    AutoRentalListDTO.class) ;
        ResponseEntity<AutoRentalListDTO> responseWithOffsetAndLimit = restTemplate.exchange(
                                                    RequestEntity.get(rentalsUriWithOffsetAndLimit).accept(mediaType).build()
                                                                    , AutoRentalListDTO.class) ;

        // then / evaluate and assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetAndLimit.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoRentalListDTO rentalsPageWithoutOffsetAndLimit = response.getBody();
        AutoRentalListDTO rentalsPageWithOffsetAndLimit = responseWithOffsetAndLimit.getBody();

        
        BDDAssertions.then(rentalsPageWithoutOffsetAndLimit.getOffset()).isEqualTo(0);
        BDDAssertions.then(rentalsPageWithoutOffsetAndLimit.getLimit()).isEqualTo(0);
        BDDAssertions.then(rentalsPageWithOffsetAndLimit.getOffset()).isEqualTo(1);
        BDDAssertions.then(rentalsPageWithOffsetAndLimit.getLimit()).isEqualTo(20);

        BDDAssertions.then(rentalsPageWithoutOffsetAndLimit.getCount()).isEqualTo(existingAutoRental.size());
        BDDAssertions.then(rentalsPageWithOffsetAndLimit.getCount()).isEqualTo(20);
        
         for (AutoRentalDTO q: rentalsPageWithoutOffsetAndLimit.getAutoRentals()) {
            BDDAssertions.then(existingAutoRental.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(existingAutoRental).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_autoRental(MediaType contentType, MediaType accept) {
        // given / arrange - a valid autoRental
        
        
        AutoRentalDTO valiAutoRentalDTO = autoRentalDTOFactory.make(validAuto,validRenter,1);

        log.info("valiAutoRentalDTO - {}", valiAutoRentalDTO);
        
        RequestEntity request =  RequestEntity.post(autoRentalUrl).accept(accept).contentType(contentType)
                                                            .body(valiAutoRentalDTO);
                                                            
        // when / act 
        ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(request, AutoRentalDTO.class);
                                                            
        
        // then 
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // that equals what we sent plus an id generated
        
        AutoRentalDTO createdAutoRental = response.getBody();
        BDDAssertions.then(createdAutoRental).isEqualTo(valiAutoRentalDTO.withId(createdAutoRental.getId()));
        // a lcation response header referencing the URL for the created resources
        URI location = UriComponentsBuilder.fromUri(baseUrl) .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(createdAutoRental.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).as("location error").isEqualTo(location.toString());
    }

    private AutoRentalDTO given_an_existing_autoRental(){
        AutoRentalDTO existingAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);
        ResponseEntity<AutoRentalDTO> response =  restTemplate.exchange(RequestEntity.post(autoRentalUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .body(existingAutoRental),AutoRentalDTO.class);
        BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void update_an_existing_autoRental_whose_timePeriod_is_not_overlapped( ){
        // given - an existing autoRental
        AutoRentalDTO existingAutoRental = given_an_existing_autoRental();
        String requestId = existingAutoRental.getId();

        // and an update 
         log.info("startDate - {} , endDate - {}", existingAutoRental.getStartDate(), existingAutoRental.getEndDate());
          AutoRentalDTO updatedAutoRental = existingAutoRental
                                            .withStartDate(existingAutoRental.getStartDate().plusDays(1))
                                            .withEndDate(existingAutoRental.getEndDate().plusDays(1))
                                            .withId(null);

        log.info("startDate - {} , endDate - {}", existingAutoRental.getStartDate(), existingAutoRental.getEndDate());

        URI updateUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(existingAutoRental.getId());

        // when - updating existing quote
        ResponseEntity<Void> response = restTemplate.exchange(RequestEntity.put(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .body(updatedAutoRental), Void.class);
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        ResponseEntity<AutoRentalDTO> getUpdatedAutoRental = restTemplate.exchange(RequestEntity.get(getUri).build(),AutoRentalDTO.class);

        BDDAssertions.then(getUpdatedAutoRental.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedAutoRental.getBody()).isEqualTo(updatedAutoRental.withId(existingAutoRental.getId()));
        BDDAssertions.then(getUpdatedAutoRental.getBody()).isNotEqualTo(existingAutoRental);

    }

    @Test
    void get_autoRental_1() {
        // given / arrange
        AutoRentalDTO existingAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);
        ResponseEntity<AutoRentalDTO> response =restTemplate.exchange(
                            RequestEntity.post(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri())
                                    .body(existingAutoRental),AutoRentalDTO.class);
                                    
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act

        ResponseEntity<AutoRentalDTO> getAutoRental = restTemplate.exchange( 
                RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl)
                .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId)).build(),AutoRentalDTO.class);

        // then
        BDDAssertions.then(getAutoRental.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getAutoRental.getBody()).isEqualTo(existingAutoRental.withId(requestId));
    }

    protected List<AutoRentalDTO> given_many_autoRentals(int count) {
        List<AutoRentalDTO> autoRentals = new ArrayList<>(count);
        for (AutoRentalDTO autoRentalDTO : autoRentalDTOFactory.listBuilder().autoRentals(count,count,validAuto,validRenter)) {
                ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(RequestEntity
                                    .post(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri())
                                    .body(autoRentalDTO),AutoRentalDTO.class);

                BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                autoRentals.add(response.getBody());
        }
        return autoRentals;
    }

    @Test
    void remove_autoRental(){
        // given 
        List<AutoRentalDTO> autoRentals = given_many_autoRentals(5);
        String requestId = autoRentals.get(1).getId();
        BDDAssertions.assertThat(restTemplate.exchange(
            RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId)).build()
                                    ,AutoRentalDTO.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        // when requested to remove
        ResponseEntity<Void> resp = restTemplate.exchange(
                    RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId)).build()
                                    ,Void.class);
        // then

        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        URI autoUri = UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        log.info("requestUri - {}, requiestId -{}", autoUri.toString(), requestId);
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () ->restTemplate.exchange(
                                             RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId)).build(),AutoRentalDTO.class) ,
                                             RestClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void remove_all_autoRentals() {
        // given  / arrange
        List<AutoRentalDTO> autoRentals = given_many_autoRentals(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = restTemplate.exchange(
                        RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH)
                                    .build().toUri()).build(),Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (AutoRentalDTO autoRentalDTO : autoRentals) {
               RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()->restTemplate.exchange(RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH)
                        .build(autoRentalDTO.getId())).build(),AutoRentalDTO.class),
                         RestClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


    @Test
    void remove_unknown_autoRental() {
        // given 
        String requestId = "13";

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = restTemplate.exchange(
                        RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH)
                        .build(requestId)).build(),Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }


    @Test
    void get_random_autoRental_no_autoRentals(){
        // given
        BDDAssertions.assertThat(restTemplate.exchange(
            RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH)
                        .build().toUri()).build(),Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // then
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> restTemplate.exchange(
            RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH)
                            .build("autoRental-1")).build(),AutoRentalDTO.class), RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains("autoRental[autoRental-1] not found");
        
    }

    @Test
    void get_unknown_autoRental(){
        // given
        String unknownId =  "autoRental-13";

        // when - requesting auto by id

        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () ->restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH)
                                .build(unknownId)).build() ,AutoRentalDTO.class)        
                                , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("autoRental[%s] not found", unknownId));

    }


    @Test
    void update_unknown_autoRental() {
        // given

        String unknownId = "autoRental-13";
        AutoRentalDTO updateAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);

        // verify that updating existing quote
        RestClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()-> restTemplate.exchange(
                RequestEntity.put(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(unknownId))
                                .body(updateAutoRental),Void.class) , RestClientResponseException.class);

        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("autoRental-[%s] not found", unknownId));
    }

    @Test
    //@Disabled
    void update_known_auto_with_bad_autoRental() {
        // given
        List<AutoRentalDTO> autoRentals = given_many_autoRentals(3);
        
        String knownId = autoRentals.get(0).getId();
        // AutoRentalDTO badAutoRentalMissingText = new AutoRentalDTO();
        AutoRentalDTO badAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);
        badAutoRental.setStartDate(LocalDate.now().minusDays(5));
        badAutoRental.withId(knownId);

        
        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> restTemplate.exchange(
                RequestEntity.put(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(knownId))
                            .body(badAutoRental),Void.class)  , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getDescription()).contains(String.format("auto rental is not valid", knownId));
    }

    @Test
    //@Disabled
    void add_bad_autoRental_rejected() {
        // given
        
        //AutoRentalDTO badAutoRentalMissingText = new AutoRentalDTO();
         AutoRentalDTO badAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);
        badAutoRental.setStartDate(LocalDate.now().minusMonths(2));

        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
        
        () -> restTemplate.postForEntity(autoRentalUrl, badAutoRental, AutoRentalDTO.class)
                    , RestClientResponseException.class);
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
        Integer offset = 0;
        Integer limit = 100;
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", limit);
        }
        URI url = urlBuilder.build().toUri();

        
         //when - asked for amounts we do not have
        ResponseEntity<AutoRentalListDTO> response = restTemplate.getForEntity(url, AutoRentalListDTO.class);
        log.debug("{}", response);

        //then - the response will be empty
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoRentalListDTO returnedAutoRentals = response.getBody();
        BDDAssertions.then(returnedAutoRentals.getCount()).isEqualTo(0);
        //and descriptive attributes filed in
        BDDAssertions.then(returnedAutoRentals.getOffset()).isEqualTo(0);
        BDDAssertions.then(returnedAutoRentals.getLimit()).isEqualTo(100);
        BDDAssertions.then(returnedAutoRentals.getTotal()).isEqualTo(0);       

    }

    @Test
    void get_many_autoRentals() {
        // given many quotes
        given_many_autoRentals(100);

        //when asking for a page of quotes
        Integer offset = 9;
        Integer limit = 10;
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("pageNumber", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("pageSize", limit);
        }
        URI url = urlBuilder.build().toUri();

        ResponseEntity<AutoRentalListDTO> response = restTemplate.getForEntity(url, AutoRentalListDTO.class);

         //then - page of results returned
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoRentalListDTO returnedAutoRentals = response.getBody();
        log.debug("{}", returnedAutoRentals);
        BDDAssertions.then(returnedAutoRentals.getCount()).isEqualTo(10);
        AutoRentalDTO auto0 = returnedAutoRentals.getAutoRentals().get(0);
        String[] ids = auto0.getId().split("-");
        
        BDDAssertions.then(Integer.valueOf(ids[1])).isGreaterThan(1);
        

        //and descriptive attributes filed in
        BDDAssertions.then(returnedAutoRentals.getOffset()).isEqualTo(9);
        BDDAssertions.then(returnedAutoRentals.getLimit()).isEqualTo(10);
        BDDAssertions.then(returnedAutoRentals.getTotal()).isEqualTo(10);
    }


}
