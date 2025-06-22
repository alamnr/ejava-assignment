package info.ejava.assignments.api.autorenters.svc.autorentals;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.SearchParams;

public interface AutoRentalService {
    AutoRentalDTO createAutoRental(AutoRentalDTO  newAutoRental);
    AutoRentalDTO getAutoRental(String id);
    boolean hasAutoRental(String id);
    AutoRentalDTO updateAutoRental(String id, AutoRentalDTO auto);
    Page<AutoRentalDTO> queryAutoRental(AutoRentalDTO probe, Pageable pageable);
    Page<AutoRentalDTO> searchAutoRental(SearchParams searchParams, Pageable pageable);
    void removeAutoRental(String id);
    void removeAllAutoRental();
}
