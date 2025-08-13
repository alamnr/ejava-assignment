package info.ejava.assignments.api.autorenters.svc.rentals;

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.SearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * This interface defines the RentalDTO marker factory and interface calls
 * as well as server API calls required from the solution-specific implementation
 * in order to perform the provided API integration tests.
 */
public interface ApiTestHelper<T extends RentalDTO> {
    ApiTestHelper<T> withRestTemplate(RestTemplate restTemplate);

    /**
     * This is a POJO/DTO factory for a proposal that can be used
     * to express the key points of a rental. It is not a remote call.
     * @param auto only auto.id used
     * @param renter only renter.id used
     * @param timePeriod startDate, endDate, and duration days
     * @return rental object suitable for query and contracting a rental
     */
    T makeProposal(AutoDTO auto, RenterDTO renter, TimePeriod timePeriod);
    T makePopulatedFake(); //create instance with non-null, fake values to test getters


    //autoRental
    String getRentalId(T autoRental);
    void setRentalId(T autoRental, String rentalId);
    String getAutoId(T autoRental);
    void setAutoId(T autoRental, String autoId);
    String getRenterId(T autoRental);
    void setRenterId(T autoRental, String renterId);
    LocalDate getStartDate(T autoRental);
    void setStartDate(T autoRental, LocalDate startDate);
    LocalDate getEndDate(T autoRental);
    void setEndDateDate(T autoRental, LocalDate endDate);

    String getAutoMakeModel(T autoRental);
    String getRenterName(T autoRental);
    BigDecimal getAmount(T autoRental);
    int getRenterAge(T autoRental);
    StreetAddressDTO getStreetAddress(T autoRental);
    default TimePeriod getTimePeriod(T autoRental) {
        return new TimePeriod(getStartDate(autoRental), getEndDate(autoRental));
    }

    ResponseEntity<T> createContract(T proposedRental);
    ResponseEntity<T> modifyContract(T proposedRental);
    ResponseEntity<T> getRental(T rentalContract);
    ResponseEntity<T> getRentalById(String rentalId);
    List<T> findRentalsBy(SearchParams searchParams);
    ResponseEntity<Void> removeRental(String rentalId);
    ResponseEntity<Void> removeAllRentals();
}
