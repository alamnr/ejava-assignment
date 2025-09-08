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


    @PreAuthorize("isAuthenticated()")
    @Override
    public AutoRentalDTO createAutoRental(AutoRentalDTO newAutoRental) {
        // 3.c.i &&  3.c.iii
        boolean userNameisBlankOrEmptyOrNull = newAutoRental.getUserName() == null || newAutoRental.getUserName().isBlank() 
                                                        || newAutoRental.getUserName().isEmpty() ;
        String ownername = userNameisBlankOrEmptyOrNull ? rentersServiceImpl.getRenter(newAutoRental.getRenterId()).getUsername() 
                                                        : newAutoRental.getUserName() ;
                           
        
        // boolean isOwner = authzHelper.isUsername(ownername);
        authzHelper.assertRules( () -> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("PROXY"), 
                        username -> String.format("%s is not this renter owner and or authorize to create autoRental for this renter[%s] ",username,newAutoRental.getRenterId()));
        return impl.createAutoRental(newAutoRental);
    }

    
    @Override
    public AutoRentalDTO getAutoRental(String id) {
        AutoRentalDTO autoRentalDTO = impl.getAutoRental(id);
        boolean userNameisBlankOrEmptyOrNull = autoRentalDTO.getUserName() == null || autoRentalDTO.getUserName().isBlank() 
                                                        || autoRentalDTO.getUserName().isEmpty() ;
        String ownername = userNameisBlankOrEmptyOrNull ? rentersServiceImpl.getRenter(autoRentalDTO.getRenterId()).getUsername() 
                                                        : autoRentalDTO.getUserName() ;
        log.info("************************************* ownername in getAutoRental - {}", ownername);
        // log.info("****************************** isUsername - {}", authzHelper.isUsername(ownername));
        // log.info("****************************** assertMgr - {}", authzHelper.assertMgr());
        // log.info("****************************** assertProxy - {}, hasAuthority - {}", authzHelper.assertProxy(), authzHelper.hasAuthority("PROXY"));
        log.info("****************************** hasAuthority - {}", authzHelper.hasAuthority("PROXY"));
        log.info("****************************** hasAuthority - {}", authzHelper.hasAuthority("ROLE_MGR"));
        // programmatic authorization constraint, in this case role inheritance do not work
        authzHelper.assertRules(()-> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("ROLE_MGR") 
                                        || authzHelper.hasAuthority("PROXY") || authzHelper.hasAuthority("ROLE_ADMIN"),
                               username -> String.format("%s is not this autorental and or authorized to get this autoRental[%s]",username,id));
        return autoRentalDTO;
    }

    @Override
    public boolean hasAutoRental(String id) {
        return impl.hasAutoRental(id);
    }


    @PreAuthorize("isAuthenticated()")
    @Override
    public AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental) {
        // 3.c.ii  &&  3.c.iii
        boolean userNameisBlankOrEmptyOrNull = autoRental.getUserName() == null || autoRental.getUserName().isBlank() 
                                                        || autoRental.getUserName().isEmpty() ;
        String ownername = userNameisBlankOrEmptyOrNull ? rentersServiceImpl.getRenter(autoRental.getRenterId()).getUsername() 
                                                                : autoRental.getUserName() ;
        authzHelper.assertRules(()-> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("PROXY"),  
                                username -> String.format("%s is not this auto renter owner and or authorize to update this autorental[%s]", username,autoRental.getId()));
        return impl.updateAutoRental(id, autoRental);
    }

    @PreAuthorize("isAuthenticated()")    // declacrative  authorization  constraint
    @Override
    public void removeAutoRental(String id) {
        // 3.c.iv and 3.c.v
        try {
            AutoRentalDTO autoRentalDTO = impl.getAutoRental(id);
            boolean userNameisBlankOrEmptyOrNull = autoRentalDTO.getUserName() == null || autoRentalDTO.getUserName().isBlank() 
                                                        || autoRentalDTO.getUserName().isEmpty() ;
                                                        
            String ownername =  userNameisBlankOrEmptyOrNull ? rentersServiceImpl.getRenter(autoRentalDTO.getRenterId()).getUsername() 
                                                                : autoRentalDTO.getUserName() ;
            log.info("********************************* ownername - {}", ownername);
            // authzHelper.assertRules(() -> authzHelper.isUsername(ownername) || authzHelper.assertMgr(),
            //                     username -> String.format("%s is not this autorenter owner ",   username));

            // programmatic authorization constraint
            authzHelper.assertRules(() -> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("ROLE_MGR") 
                                            || authzHelper.hasAuthority("ROLE_ADMIN"),
                                username -> String.format("%s is not this autorenter owner ",   username));
            impl.removeAutoRental(id);    
        } catch (ClientErrorException.NotFoundException ex) {
            /* already does not exist -- no error */
        }
        
    }

    //  annotation based declarative authorization constraint  
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")  // 3.c.vi  
    @Override
    public void removeAllAutoRental() {
        // 3.c.vi
        // authzHelper.assertRules(() -> authzHelper.isAuthenticated() && authzHelper.assertAdmin(),
        //                         username -> String.format("%s is not authorize to delete all autorentals", username));

        // or , cause authzHelper.assertAdmin() / authzHelper.assertMgr() / authzHelper.assertProxy() always return true, 
        // i think it does not work properly

        // programmatic authorization constraint
        // authzHelper.assertRules(() -> authzHelper.isAuthenticated() && authzHelper.hasAuthority("ROLE_ADMIN"),
        //                          username -> String.format("%s is not authorize to delete all autorentals", username));

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
