package info.ejava.assignments.api.autorentals.svc.main.rental.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.rental.ApiTestHelper;
import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsAPI;
import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.examples.common.web.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

//TODO: implement this component for MyAutoRentalsAPINTest
/**
 * This class maps the RentalDTO marker factory and interface calls as well
 * as server API calls to solution-specific methods.
 *
 * "make" methods are client-side POJO factories.
 * "getter/setter" methods translate between requests and your concrete rental DTO class.
 * server-side calls return ResponseEntity except the finder(s)
 * "finder(s)" simply return the List of DTOs
 */

@Slf4j
public class ApiTestHelperImpl implements ApiTestHelper<AutoRentalDTO> {
    //you will need a client instance to call your server-side implementation

    private final RestTemplate restTemplate;
    private final ServerConfig serverConfig;
    
    public ApiTestHelperImpl(RestTemplate restTemplate, ServerConfig serverConfig){
        this.restTemplate = restTemplate;
        this.serverConfig = serverConfig;
    }

    //you may need a reusable mechanism to construct DTO instances

    @Override
    public ApiTestHelper<AutoRentalDTO> withRestTemplate(RestTemplate restTemplate) {
        return new ApiTestHelperImpl(restTemplate, this.serverConfig); //new instance of this helper, with clients using provided restTemplate
    }

    @Override
    public AutoRentalDTO makeProposal(AutoDTO auto, RenterDTO renter, TimePeriod timePeriod) {
        return new AutoRentalDTO(auto, renter, timePeriod);
    }
    public AutoRentalDTO makePopulatedFake() { //create instance with non-null, fake values to test getters
        AutoRentalDTO dto = new AutoRentalDTO();
        dto.setId("RENT123");
        dto.setAutoId("AUTO123");
        dto.setRenterId("RENTER123");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(7));
        dto.setAmount(BigDecimal.valueOf(299.99));
        return dto;
        
    }

    @Override
    public String getRentalId(AutoRentalDTO autoRental) {
        return autoRental.getId();
    }

    @Override
    public void setRentalId(AutoRentalDTO autoRental, String rentalId) {
        autoRental.setId(rentalId);
    }

    @Override
    public String getAutoId(AutoRentalDTO autoRental) {
        return autoRental.getAutoId();
    }

    @Override
    public void setAutoId(AutoRentalDTO autoRental, String autoId) {
        autoRental.setAutoId(autoId);
    }

    @Override
    public String getRenterId(AutoRentalDTO autoRental) {
        return autoRental.getRenterId();
    }

    @Override
    public void setRenterId(AutoRentalDTO autoRental, String renterId) {
        autoRental.setRenterId(renterId);
    }

    @Override
    public LocalDate getStartDate(AutoRentalDTO autoRental) {
        return autoRental.getStartDate();
    }
    @Override
    public void setStartDate(AutoRentalDTO autoRental, LocalDate startDate) {
        autoRental.setStartDate(startDate);
    }

    @Override
    public LocalDate getEndDate(AutoRentalDTO autoRental) {
        return autoRental.getEndDate();
    }
    @Override
    public void setEndDateDate(AutoRentalDTO autoRental, LocalDate endDate) {
        autoRental.setEndDate(endDate);
    }

    @Override
    public String getAutoMakeModel(AutoRentalDTO autoRental) {
        return autoRental.getMakeModel();
    }

    @Override
    public String getRenterName(AutoRentalDTO autoRental) {
        return autoRental.getRenterName();
    }

    @Override
    public BigDecimal getAmount(AutoRentalDTO autoRental) {
        return autoRental.getAmount();
    }

    @Override
    public int getRenterAge(AutoRentalDTO autoRental) {
        return autoRental.getRenterAge();
    }

    @Override
    public StreetAddressDTO getStreetAddress(AutoRentalDTO autoRental) {
        return autoRental.getStreetAddress();
    }

    @Override
    public TimePeriod getTimePeriod(AutoRentalDTO autoRental) {
        return new TimePeriod(autoRental.getStartDate(), autoRental.getEndDate());
    }

    ////////// calls to server-side API


    @Override
    public ResponseEntity<AutoRentalDTO> createContract(AutoRentalDTO proposedRental) {

        URI url = UriComponentsBuilder.fromUri(this.serverConfig.getBaseUrl()).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri();
        // RequestEntity<AutoRentalDTO> request = RequestEntity
        //                                         .post(url)
        //                                         .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        //                                         .body(proposedRental);
                                            
        // log.info("request body  - {}", request.getBody());
        // log.info("request http Method - {}", request.getMethod());
        // log.info("request content type - {}", request.getHeaders().getContentType());
        // log.info("request accept type - {}", request.getHeaders().getAccept());

        // ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(request, AutoRentalDTO.class);

        // log.info("resp. body - {}", response.getBody());
        // log.info("resp. content type - {}", response.getHeaders().getContentType());

        // return response;

        return restTemplate.postForEntity(url, proposedRental, AutoRentalDTO.class);
    }

    @Override
    public ResponseEntity<AutoRentalDTO> modifyContract(AutoRentalDTO proposedRental) {
        URI url = UriComponentsBuilder.fromUri(this.serverConfig.getBaseUrl()).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(proposedRental.getId());
        restTemplate.put(url,proposedRental);
        return getRentalById(proposedRental.getId()); 
    }

    @Override
    public ResponseEntity<AutoRentalDTO> getRental(AutoRentalDTO rentalContract) {
        return getRentalById(rentalContract.getId());
    }

    @Override
    public ResponseEntity<AutoRentalDTO> getRentalById(String rentalId) {
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(rentalId);
        return restTemplate.getForEntity(url, AutoRentalDTO.class);
    }

    @Override
    public List<AutoRentalDTO> findRentalsBy(RentalSearchParams searchParams) {

        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path(AutoRentalsAPI.AUTO_RENTAL_QUERY_PATH).build().toUri();
        ResponseEntity<AutoRentalListDTO> response = restTemplate.postForEntity(url, searchParams, AutoRentalListDTO.class);
        return response.getBody().getAutoRentals();
    }

    @Override
    public ResponseEntity<AutoRentalListDTO> findRentals(RentalSearchParams searchParams) {

        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path(AutoRentalsAPI.AUTO_RENTAL_QUERY_PATH).build().toUri();
        return restTemplate.postForEntity(url, searchParams, AutoRentalListDTO.class);
    }

    @Override
    public ResponseEntity<Void> removeRental(String rentalId) {
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path(AutoRentalsAPI.AUTO_RENTAL_PATH).build(rentalId);
        return  restTemplate.exchange(RequestEntity.delete(url).build(), Void.class);
    }

    @Override
    public ResponseEntity<Void> removeAllRentals() {
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).path(AutoRentalsAPI.AUTO_RENTALS_PATH).build().toUri();
        return restTemplate.exchange(RequestEntity.delete(url).build(), Void.class);
    }
}
