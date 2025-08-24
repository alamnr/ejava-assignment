package info.ejava.alamnr.assignment3.security.autorentals;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NoSecurityAutorentalsServiceWrapper implements AutoRentalService {
    
    private final AutoRentalService impl;

    @Override
    public AutoRentalDTO createAutoRental(AutoRentalDTO newAutoRental) {
        newAutoRental.setUserName("anonymous");
        return impl.createAutoRental(newAutoRental);
    }

    @Override
    public AutoRentalDTO getAutoRental(String id) {
        return impl.getAutoRental(id);
    }

    @Override
    public boolean hasAutoRental(String id) {
        return impl.hasAutoRental(id);
    }

    @Override
    public AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental) {
        return impl.updateAutoRental(id, autoRental);
    }

    @Override
    public void removeAutoRental(String id) {
        impl.removeAutoRental(id);
    }

    @Override
    public void removeAllAutoRental() {
        impl.removeAllAutoRental();
    }

    @Override
    public Page<AutoRentalDTO> queryAutoRental(AutoRentalDTO probe, Pageable pageable) {
        return impl.queryAutoRental(probe, pageable);
    }

    @Override
    public Page<AutoRentalDTO> searchAutoRental(RentalSearchParams searchParams, Pageable pageable) {
        return impl.searchAutoRental(searchParams, pageable);
    }

    @Override
    public Page<AutoRentalDTO> getAutoRentals(Pageable pageable) {
        return impl.getAutoRentals(pageable);
    }

    @Override
    public Page<AutoRentalDTO> findByRenterName(String renterName, Pageable pageable) {
        return impl.findByRenterName(renterName, pageable);
    }
}
