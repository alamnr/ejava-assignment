package info.ejava.assignments.api.autorenters.client.renters;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

/**
 * This is an implementation of a Spring 6 HTTP Interface, which
 * provides a client-side definition for HTTP calls versus writing
 * the RestTemplate/RestClient calls manually.
 * Since annotations are hard-wired within code, this version of the
 * interface was written to address JSON requests/responses.
 */
public interface RentersJSONHttpIfaceMapping extends RentersHttpIface {

    @Override
    @PostExchange(url= RENTERS_PATH,
            contentType = MediaType.APPLICATION_JSON_VALUE,
            accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RenterDTO> createRenter(@RequestBody RenterDTO renter);

    @Override
    @GetExchange(url= RENTERS_PATH, accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RenterListDTO> getRenters(
            @RequestParam(name="pageNumber", required = false)
            Integer pageNumber,
            @RequestParam(name="pageSize", required = false)
            Integer pageSize);

    @Override
    @GetExchange(url = RENTER_PATH, accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RenterDTO> getRenter(@PathVariable("id") String id);

    @Override
    @HttpExchange(method = "HEAD", url=RENTER_PATH)
    ResponseEntity<Void> hasRenter(@PathVariable("id") String id);

    @Override
    @PutExchange(url=RENTER_PATH,
            contentType = MediaType.APPLICATION_JSON_VALUE,
            accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RenterDTO> updateRenter(@PathVariable("id") String id,
                                           @RequestBody RenterDTO renter);

    @Override
    @DeleteExchange(RENTER_PATH)
    ResponseEntity<Void> removeRenter(@PathVariable("id") String id);

    @Override
    @DeleteExchange(RENTERS_PATH)
    ResponseEntity<Void> removeAllRenters();
}
