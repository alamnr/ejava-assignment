package info.ejava.assignments.security.autorenters.svc.autorentals;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
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
public class SecureAutoRentalServiceWrapper implements AutoRentalService {

    private final  AutoRentalService impl;
    private final AutosService autosServiceImpl;
    private final RentersService rentersServiceImpl;
    private final AuthorizationHelper authzHelper;


    @Override
    public AutoRentalDTO createAutoRental(AutoRentalDTO newAutoRental) {
        // 3.c.i &&  3.c.iii
        String ownername = newAutoRental.getRenterName()==null ?  rentersServiceImpl.getRenter(newAutoRental.getRenterId()).getUsername()
                                                                : newAutoRental.getRenterName();
        
        // boolean isOwner = authzHelper.isUsername(ownername);
        authzHelper.assertRules( () -> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("PROXY"), 
                        username -> String.format("%s is not this renter owner and or authorize to create autoRental for this renter[%s] ",username,newAutoRental.getRenterId()));
        return impl.createAutoRental(newAutoRental);
    }

    @Override
    public AutoRentalDTO getAutoRental(String id) {
        AutoRentalDTO autoRentalDTO = impl.getAutoRental(id);
        String ownername = autoRentalDTO.getUserName();
        authzHelper.assertRules(()-> authzHelper.isUsername(ownername) || authzHelper.assertMgr(),
                               username -> String.format("%s is not this autorental and or authorized to get this autoRental[%s]",username,id));
        return autoRentalDTO;
    }

    @Override
    public boolean hasAutoRental(String id) {
        return impl.hasAutoRental(id);
    }

    @Override
    public AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental) {
        // 3.c.ii  &&  3.c.iii
        String ownername = autoRental.getUserName();
        authzHelper.assertRules(()-> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("PROXY"),  
                                username -> String.format("%s is not this auto renter owner and or authorize to update this autorental[%s]", username,autoRental.getId()));
        return impl.updateAutoRental(id, autoRental);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public void removeAutoRental(String id) {
        // 3.c.iv and 3.c.v
        try {
            AutoRentalDTO autoRentalDTO = impl.getAutoRental(id);
            String ownername = autoRentalDTO.getUserName();
            authzHelper.assertRules(() -> authzHelper.isUsername(ownername) || authzHelper.assertMgr(),
                                username -> String.format("%s is not this autorenter owner ",   username));
            impl.removeAutoRental(id);    
        } catch (ClientErrorException.NotFoundException ex) {
            /* already does not exist -- no error */
        }
        
    }

    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")  // 3.c.vi
    @Override
    public void removeAllAutoRental() {
        // 3.c.vi
        // authzHelper.assertRules(() -> authzHelper.isAuthenticated() && authzHelper.assertAdmin(),
        //                         username -> String.format("%s is not authorize to delete all autorentals", username));
        impl.removeAllAutoRental();;
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
