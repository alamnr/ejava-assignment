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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
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

@SpringBootTest(classes={AutoRentalTestConfiguration.class,AutoRentalsAppMain.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRentalRestClientNTest {
    
    @Autowired 
    private RestClient restClient;

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
            for(MediaType acceptType : MEDIA_TYPES) {
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
        ResponseEntity<Void> responseAutoDelete = restClient.delete()
                                                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri())
                                                    .retrieve().toEntity(Void.class);
        ResponseEntity<Void> responseRenterDelete = restClient.delete()
                                                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH).build().toUri())
                                                    .retrieve().toEntity(Void.class);                            
        ResponseEntity<Void> responseAutoRentalDelete = restClient.delete().uri(autoRentalUrl).retrieve().toEntity(Void.class) ;  

        BDDAssertions.then(responseAutoDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        BDDAssertions.then(responseRenterDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        BDDAssertions.then(responseAutoRentalDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);



        // insert a valid auto having id - "auto-1" and renter having id - "renter-1" 
        URI autoUri = UriComponentsBuilder.fromUri(baseUrl).path(AutosAPI.AUTOS_PATH).build().toUri();
        ResponseEntity<AutoDTO> responseAuto = restClient.post().uri(autoUri)
                                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                                .body(validAuto).retrieve()
                                                .toEntity(AutoDTO.class);
        BDDAssertions.then(responseAuto.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        log.info("created Auto - {}", responseAuto.getBody());
        validAuto = responseAuto.getBody();

        URI renterUri = UriComponentsBuilder.fromUri(baseUrl).path(RentersAPI.RENTERS_PATH).build().toUri();
        ResponseEntity<RenterDTO> responseRenter = restClient.post().uri(renterUri)
                                                    .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                                    .body(validRenter).retrieve()
                                                    .toEntity(RenterDTO.class);
        BDDAssertions.then(responseRenter.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        log.info("created Renter - {}", responseRenter.getBody());
        validRenter = responseRenter.getBody();
    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_autoRental_for(MediaType contentType, MediaType accept){

        // given - a valid auto
      
       
        AutoRentalDTO validAutoRentalDTO = autoRentalDTOFactory.make(validAuto,validRenter,1);
        log.info("Content-Type-{}, Accept-Type-{}, auto -{}", contentType, accept, validAutoRentalDTO);

        // when - making a request with different content and accept payload types
        RequestEntity <AutoRentalDTO> request = RequestEntity
                                            .post(autoRentalUrl)
                                            .contentType(contentType)
                                            .accept(accept)
                                            .body(validAutoRentalDTO);

        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        
        ResponseEntity<AutoRentalDTO> response = restClient.post().uri(autoRentalUrl)
                                            .contentType(contentType).accept(accept)
                                            .body(validAutoRentalDTO).retrieve()
                                            .toEntity(AutoRentalDTO.class);

        log.info("resp. body - {}", response.getBody());
        log.info("resp. content type - {}", response.getHeaders().getContentType());
        
        // then - the service will accept the for(mat we supplied
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BDDAssertions.then(response.getHeaders().getContentType()).isEqualTo(accept);
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo(accept.toString());

        // that equals what we sent and plus an ID generated
        AutoRentalDTO createdAutoRental = response.getBody();
        BDDAssertions.then(createdAutoRental).isEqualTo(validAutoRentalDTO.withId(createdAutoRental.getId()));
        // with a location reponse header referencing the URI of the created auto
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(createdAutoRental.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());
        
    }

    

    @Test
    void get_autoRental(){
        // given/ arrange - an existing autoRental
        final TimePeriod timePeriod = new TimePeriod(validAutoRental.getStartDate(), 
                                validAutoRental.getEndDate() != null ? validAutoRental.getEndDate() : validAutoRental.getStartDate());
        final AutoRentalDTO existingAutoRental = new AutoRentalDTO(validAuto, validRenter, timePeriod) ;
        ResponseEntity<AutoRentalDTO> response = restClient.post()
                                            .uri(autoRentalUrl).body(existingAutoRental)
                                            .retrieve().toEntity( AutoRentalDTO.class);
        BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();        

        String requestId = response.getBody().getId();
        
        URI autoRentalUrl = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(autoRentalUrl);
        
        // when / act - requesting auto get by id
        ResponseEntity<AutoRentalDTO> autoRentalResponse  = restClient.get().uri(autoRentalUrl).retrieve().toEntity(AutoRentalDTO.class);
        
        // then
        BDDAssertions.then(autoRentalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(autoRentalResponse.getBody()).isEqualTo(existingAutoRental.withId(requestId));

    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void get_autoRentals(String mediaTypeString){
        // given / arrange
        log.info("mediaTypeString - {}", mediaTypeString);
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String, AutoRentalDTO> existingAutoRentals = new HashMap<>();
        AutoRentalListDTO autoRentals = autoRentalDTOFactory.listBuilder().make(40, 40,validAuto,validRenter);
        for(  AutoRentalDTO autoRental : autoRentals.getAutoRentals()) {
            ResponseEntity<AutoRentalDTO> response = restClient.post().uri(autoRentalUrl).contentType(mediaType)
                                                .body(autoRental).retrieve().toEntity( AutoRentalDTO.class);
            BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            AutoRentalDTO addedautoRental = response.getBody();
            existingAutoRentals.put(addedautoRental.getId(), addedautoRental);
        }
        BDDAssertions.assertThat(existingAutoRentals).isNotEmpty();
        URI autoRentalsUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri();
        URI autoRentalsUriWithOffsetAndpageSize = UriComponentsBuilder.fromUri(baseUrl)
                                        .path(AutoRentalsAPI.AUTO_RENTALS_PATH)
                                        .queryParam("pageNumber", 1)
                                        .queryParam("pageSize", 20)
                                        .build().toUri();

        // when / act
        
        ResponseEntity<AutoRentalListDTO> response = restClient.get().uri(autoRentalsUri)
                                                .accept(mediaType).retrieve().toEntity(AutoRentalListDTO.class);
        
        ResponseEntity<AutoRentalListDTO> responseWithOffsetAndpageSize = restClient.get().uri(autoRentalsUriWithOffsetAndpageSize)
                                                                    .accept(mediaType).retrieve().toEntity(AutoRentalListDTO.class);
                                            

        // then / evaluate and assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetAndpageSize.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoRentalListDTO autoRentalPageWithoutOffsetAndpageSize = response.getBody();
        AutoRentalListDTO autoRentalPageWithOffsetAndpageSize = responseWithOffsetAndpageSize.getBody();

        
        BDDAssertions.then(autoRentalPageWithoutOffsetAndpageSize.getOffset()).isEqualTo(0);
        BDDAssertions.then(autoRentalPageWithoutOffsetAndpageSize.getLimit()).isEqualTo(0);
        BDDAssertions.then(autoRentalPageWithOffsetAndpageSize.getOffset()).isEqualTo(1);
        BDDAssertions.then(autoRentalPageWithOffsetAndpageSize.getLimit()).isEqualTo(20);

        BDDAssertions.then(autoRentalPageWithoutOffsetAndpageSize.getCount()).isEqualTo(existingAutoRentals.size());
        BDDAssertions.then(autoRentalPageWithOffsetAndpageSize.getCount()).isEqualTo(20);
        
         for(  AutoRentalDTO q: autoRentalPageWithoutOffsetAndpageSize.getAutoRentals()) {
            BDDAssertions.then(existingAutoRentals.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(existingAutoRentals).isEmpty();

    }

    
    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_autoRental(MediaType contentType, MediaType accept) {
        // given / arrange - a valid auto
        AutoRentalDTO validAutoRentalDTO = autoRentalDTOFactory.make(validAuto,validRenter,1);
        // when / act 
        ResponseEntity<AutoRentalDTO> response = restClient.post().uri(autoRentalUrl)
                                            .contentType(contentType).body(validAutoRentalDTO)
                                            .retrieve().toEntity( AutoRentalDTO.class);
        
        // then 
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // that equals what we sent plus an id generated
        
        AutoRentalDTO createdAutoRental = response.getBody();
        BDDAssertions.then(createdAutoRental).isEqualTo(validAutoRentalDTO.withId(createdAutoRental.getId()));
        // a lcation response header referencing the URL for( the created resources
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(createdAutoRental.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).as("location error").isEqualTo(location.toString());
    }

    private AutoRentalDTO given_an_existing_autoRental(){
     AutoRentalDTO existingAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);
        ResponseEntity<AutoRentalDTO> response =  restClient.post().uri(autoRentalUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .body(existingAutoRental)
                                                            .retrieve().toEntity( AutoRentalDTO.class);
        
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
        AutoRentalDTO updatedAutoRental = existingAutoRental
                                            .withStartDate(existingAutoRental.getStartDate().plusDays(1))
                                            .withEndDate(existingAutoRental.getEndDate().plusDays(1))
                                            .withId(null);

        log.info("startDate - {} , endDate - {}", existingAutoRental.getStartDate(), existingAutoRental.getEndDate());

        URI updateUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(existingAutoRental.getId());

        // when - updating existing quote
        ResponseEntity<Void> response = restClient.put().uri(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .body(updatedAutoRental)
                                                        .retrieve().toEntity(Void.class);
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        ResponseEntity<AutoRentalDTO> getUpdatedAutoRental = restClient.get().uri(getUri).retrieve().toEntity( AutoRentalDTO.class);

        BDDAssertions.then(getUpdatedAutoRental.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedAutoRental.getBody()).isEqualTo(updatedAutoRental.withId(existingAutoRental.getId()));
        BDDAssertions.then(getUpdatedAutoRental.getBody()).isNotEqualTo(existingAutoRental);

    }

    @Test
    void get_autoRental_1() {
        // given / arrange
        AutoRentalDTO existingAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);
        ResponseEntity<AutoRentalDTO> response = restClient.post().uri(autoRentalUrl)
                                            .body(existingAutoRental).retrieve().toEntity( AutoRentalDTO.class);
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String requestId = response.getBody().getId();

        // when / act

        ResponseEntity<AutoRentalDTO> getautoRental =  restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                                 .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                                 .retrieve().toEntity( AutoRentalDTO.class);
        

        // then
        BDDAssertions.then(getautoRental.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getautoRental.getBody()).isEqualTo(existingAutoRental.withId(requestId));
    }

    protected List <AutoRentalDTO> given_many_autoRentals(int count) {
        List <AutoRentalDTO> autoRentals = new ArrayList<>(count);
        for(  AutoRentalDTO auto : autoRentalDTOFactory.listBuilder().autoRentals(count,count,validAuto,validRenter)) {
                ResponseEntity<AutoRentalDTO> response = restClient.post().uri(autoRentalUrl).body (auto).retrieve()
                                                        .toEntity( AutoRentalDTO.class);
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
        BDDAssertions.assertThat(restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                          .retrieve().toEntity( AutoRentalDTO.class).getStatusCode()).isEqualTo(HttpStatus.OK);
            
        // when requested to remove
        ResponseEntity<Void> resp = restClient.delete().uri(UriComponentsBuilder
                                            .fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                            .retrieve().toEntity(Void.class);
        
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        URI u = UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId);
        log.info("requestUri - {}, requiestId -{}", u.toString(), requestId);
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () -> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId)).retrieve().toEntity( AutoRentalDTO.class),
                                             RestClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void remove_all_autoRentals() {
        // given  / arrange
        List <AutoRentalDTO> autoRentals = given_many_autoRentals(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = restClient.delete().uri(autoRentalUrl).retrieve().toEntity(Void.class);
                                           
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for(  AutoRentalDTO AutoRentalDTO : autoRentals) {
               RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()-> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH)
                        .build(AutoRentalDTO.getId())).retrieve().toEntity( AutoRentalDTO.class),
                         RestClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


    @Test
    void remove_unknown_autoRental() {
        // given 
        String requestId = "autoRental-13";

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = restClient.delete().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(requestId))
                                        .retrieve().toEntity(Void.class);
                                                
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }


    @Test
    void get_random_autoRental_no_autoRentals(){
        // given
        BDDAssertions.assertThat(restClient.delete().uri(autoRentalUrl)
                        .retrieve().toEntity(Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
               
        // then
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build("autoRental-1"))
                            .retrieve().toEntity( AutoRentalDTO.class),
                             RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains("autoRental[autoRental-1] not found");
        
    }

    @Test
    void get_unknown_autoRental(){
        // given
        String unknownId =  "autoRental-13";

        // when - requesting quote by id

        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(unknownId))
                                    .retrieve().toEntity( AutoRentalDTO.class),RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getDescription()).contains(String.format("autoRental[%s] not found", unknownId));

    }


    @Test
    void update_unknown_autoRental() {
        // given

     String unknownId = "autoRental-13";
     AutoRentalDTO updateauto = autoRentalDTOFactory.make(validAuto,validRenter,1);

        // verify that updating existing quoRentalte
        RestClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()->  restClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(unknownId))
                            .body(updateauto).retrieve().toEntity(Void.class), RestClientResponseException.class);

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
        AutoRentalDTO badAutoRental = autoRentalDTOFactory.make(validAuto,validRenter,1);
        badAutoRental.setStartDate(LocalDate.now().minusDays(5));
        badAutoRental.withId(knownId);
        
        
        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> restClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(knownId))
                        .body(badAutoRental).retrieve().toEntity(Void.class),
                         RestClientResponseException.class);
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
        MediaType contentType = MediaType.valueOf( MediaType.APPLICATION_XML_VALUE);        
        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(        
                                () ->  restClient.post().uri(autoRentalUrl).contentType(contentType)
                                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)).body(badAutoRental)
                                .retrieve().toEntity( AutoRentalDTO.class), RestClientResponseException.class);
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

        
         //when - asked for( amounts we do not have
        ResponseEntity<AutoRentalListDTO> response = restClient.get().uri(url).retrieve().toEntity(AutoRentalListDTO.class);
        
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

        ResponseEntity<AutoRentalListDTO> response = restClient.get().uri(url).retrieve().toEntity(AutoRentalListDTO.class);
        

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
