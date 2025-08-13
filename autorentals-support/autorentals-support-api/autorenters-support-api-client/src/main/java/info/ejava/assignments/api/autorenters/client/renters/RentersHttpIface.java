package info.ejava.assignments.api.autorenters.client.renters;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import org.springframework.http.ResponseEntity;

/**
 * This is an mapping-free implementation of a Spring 6 HTTP Interface.
 * An extending interface will define the HTTP mapping.
 */
public interface RentersHttpIface {
    String RENTERS_PATH = "/api/renters";
    String RENTER_PATH = "/api/renters/{id}";

    ResponseEntity<RenterDTO> createRenter(RenterDTO renter);

    ResponseEntity<RenterListDTO> getRenters(Integer pageNumber, Integer pageSize);

    ResponseEntity<RenterDTO> getRenter(String id);

    ResponseEntity<Void> hasRenter(String id);

    ResponseEntity<RenterDTO> updateRenter(String id, RenterDTO renter);

    ResponseEntity<Void> removeRenter(String id);

    ResponseEntity<Void> removeAllRenters();
}
