package info.ejava.assignments.api.autorenters.client.renters;

import org.springframework.web.client.RestTemplate;

/**
 * This interface extends the Spring 6 Http Interface for communicating with
 * a renter and any custom methods to be implemented by the RentersAPIClient.
 */
public interface RentersAPI extends RentersHttpIface {
    RentersAPI withRestTemplate(RestTemplate restTemplate);
}
