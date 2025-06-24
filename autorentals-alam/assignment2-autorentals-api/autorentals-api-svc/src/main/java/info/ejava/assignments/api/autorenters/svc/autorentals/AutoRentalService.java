package info.ejava.assignments.api.autorenters.svc.autorentals;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;

public interface AutoRentalService {
    
    AutoRentalDTO createAutoRental(AutoRentalDTO  newAutoRental);
    
    AutoRentalDTO getAutoRental(String id);
    
    boolean hasAutoRental(String id);
    
    AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental);
    
    void removeAutoRental(String id);
    void removeAllAutoRental();


    Page<AutoRentalDTO> queryAutoRental(AutoRentalDTO probe, Pageable pageable);
    Page<AutoRentalDTO> searchAutoRental(RentalSearchParams searchParams, Pageable pageable);

    Page<AutoRentalDTO> getAutoRentals(Pageable pageable);
    Page<AutoRentalDTO> findByRenterName(String renterName, Pageable pageable);
    
    
}
