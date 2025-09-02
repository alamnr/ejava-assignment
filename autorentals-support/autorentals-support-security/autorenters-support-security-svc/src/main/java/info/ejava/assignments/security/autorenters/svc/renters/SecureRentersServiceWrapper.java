package info.ejava.assignments.security.autorenters.svc.renters;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.assignments.security.autorenters.svc.AuthorizationHelper;
import info.ejava.examples.common.exceptions.ClientErrorException;
import info.ejava.examples.common.exceptions.ServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SecureRentersServiceWrapper implements RentersService {
    
    private final RentersService impl;
    private final AuthorizationHelper authzHelper;
    @Override
    public RenterDTO createRenter(RenterDTO newrenter) {
        // 3.b.ii
        String username = authzHelper.getUsername().orElseThrow(()->
           new ServerErrorException.InternalErrorException("Security has not been enabled")
        );
        Optional<RenterDTO> existingRenter  = impl.findRenterByUsername(username);
        if(existingRenter.isPresent()){
            throw new ClientErrorException.InvalidInputException("renter already exists for %s", username);
        }

        newrenter.setUsername(username);
      
        return impl.createRenter(newrenter);
    }

    //@PreAuthorize("isAuthenticated()")
    @Override
    public RenterDTO getRenter(String id) {
        RenterDTO renterDTO = impl.getRenter(id);
        String ownername = renterDTO.getUsername();
        boolean isOwner = authzHelper.isUsername(ownername);
        authzHelper.assertRules(()-> authzHelper.isUsername(ownername) || authzHelper.hasAuthority("PROXY") || authzHelper.assertMgr(),
                             username -> String.format("%s is not this renter and or authorized to get this renter[%s]",username,id));
        return renterDTO;
    }
    @Override
    public boolean hasRenter(String id) {
        return impl.hasRenter(id);
    }
    @Override
    public RenterDTO updateRenter(String id, RenterDTO renterDTO) {
        authzHelper.assertUsername(()->impl.getRenter(id).getUsername()); // 3.b.iii
        return impl.updateRenter(id, renterDTO);
    }
    @Override
    public Page<RenterDTO> getRenters(Pageable pageable) {
        return impl.getRenters(pageable);
    }
    @Override
    public Optional<RenterDTO> findRenterByUsername(String username) {
        return impl.findRenterByUsername(username);
    }

    //@PreAuthorize("isAuthenticated()")
    @Override
    public void removeRenter(String id) {
        // 3.b.iv
        try {
            String ownerName = impl.getRenter(id).getUsername();
            authzHelper.assertRules(() -> authzHelper.isUsername(ownerName) || authzHelper.assertMgr(),
                                    username -> String.format("%s is not renter or have MGR role", username));
            impl.removeRenter(id);
            
        } catch (ClientErrorException.NotFoundException ex) {
            /* already does not exist -- no error */
        }
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @Override
    public void removeAllRenters() {
        impl.removeAllRenters();
    }


}
