package info.ejava.assignments.api.autorenters.client.autorentals;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.SearchParams;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/**
 * This interface defines the REST-like, RMM-level3 interface to the
 * Auto Service.
 */
public interface AutoRentalsAPI {
    String AUTO_RENTALS_PATH = "/api/autorentals";
    String AUTO_RENTAL_PATH = "/api/autorentals/{id}";
    String AUTO_RENTAL_QUERY_PATH = "/api/autorentals/query";

    //AutosAPIRestTemplate withRestTemplate(RestTemplate restTemplate);

    ResponseEntity<AutoRentalDTO> createAutoRental(AutoRentalDTO autoRental);
    ResponseEntity<AutoRentalListDTO> queryAutoRental(AutoRentalDTO probe, Integer pageNumber, Integer pageSize);
    // ResponseEntity<AutoRentalListDTO> searchAutoRental(Integer pageNumber,Integer pageSize);
    ResponseEntity<AutoRentalListDTO> searchAutoRental(SearchParams searchParams);
    ResponseEntity<AutoRentalDTO> getAutoRental(String id);
    ResponseEntity<Void> hasAutoRental(String id);
    ResponseEntity<AutoRentalDTO> updateAutoRental(String id, AutoRentalDTO autoRental);
    ResponseEntity<Void> removeAutoRental(String id);
    ResponseEntity<Void> removeAllAutoRental();
}
