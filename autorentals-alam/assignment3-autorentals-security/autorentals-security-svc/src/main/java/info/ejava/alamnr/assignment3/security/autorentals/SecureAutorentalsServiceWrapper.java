package info.ejava.alamnr.assignment3.security.autorentals;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalService;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.assignments.security.autorenters.svc.AuthorizationHelper;
import info.ejava.examples.common.exceptions.ClientErrorException;
import info.ejava.examples.common.exceptions.ServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SecureAutorentalsServiceWrapper implements AutoRentalService  {
    
    private final AutoRentalService impl;
    private final AutosService autosServiceImpl;
    private final RentersService rentersServiceImpl;
    private final AuthorizationHelper authzHelper;

    @Override
    public AutoRentalDTO createAutoRental(AutoRentalDTO newAutoRental) {
        String username = authzHelper.getUsername().orElseThrow(()->
            new ServerErrorException.InternalErrorException("Security has not been enabled"));
        newAutoRental.setUserName(username);
        return impl.createAutoRental(newAutoRental);
    }

    //@PreAuthorize("isAuthenticated()")
    @Override
    public AutoRentalDTO getAutoRental(String id) {
        AutoRentalDTO autoRentalDTO = impl.getAutoRental(id);
        String ownername = autoRentalDTO.getUserName();
        boolean isOwner = authzHelper.isUsername(ownername);
        // authzHelper.assertRules(()-> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("PROXY") || authzHelper.assertMgr(),
        //                      username -> String.format("%s is not this autorental and or authorized to get this autoRental[%s]",username,id));
        return autoRentalDTO;
    }

    @Override
    public boolean hasAutoRental(String id) {
        return impl.hasAutoRental(id);
    }

    @Override
    public AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental) {
        authzHelper.assertUsername(()-> impl.getAutoRental(id).getUserName());
        return impl.updateAutoRental(id, autoRental);
    }

    //@PreAuthorize("isAuthenticated()")
    @Override
    public void removeAutoRental(String id) {
        try {
            String ownerName = impl.getAutoRental(id).getUserName();
            authzHelper.assertRules(() -> authzHelper.isUsername(ownerName) || authzHelper.assertMgr(),
                                    username -> String.format("%s is not autoRental owner or have MGR role", username));
            impl.removeAutoRental(id);
            
        } catch (ClientErrorException.NotFoundException ex) {
            /* already does not exist -- no error */
        }
    }

    //@PreAuthorize("hasRole('ADMIN')")
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
