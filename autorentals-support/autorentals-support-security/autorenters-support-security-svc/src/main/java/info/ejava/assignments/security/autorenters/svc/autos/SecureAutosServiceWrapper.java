package info.ejava.assignments.security.autorenters.svc.autos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.assignments.security.autorenters.svc.AuthorizationHelper;
import info.ejava.examples.common.exceptions.ClientErrorException;
import info.ejava.examples.common.exceptions.ServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SecureAutosServiceWrapper implements AutosService {
    private final AutosService impl;
    private final AuthorizationHelper authzHelper;
    @Override
    public AutoDTO createAuto(AutoDTO newAuto) {
        String username = authzHelper.getUsername().orElseThrow(() -> 
            new ServerErrorException.InternalErrorException("Security has not being enabled.")
        );
        log.debug("{} called addAuto", username);
        // username set by authenticated login
        newAuto.setUsername(username);
        return impl.createAuto(newAuto);
    }
    @Override
    public AutoDTO getAuto(String id) {
        return impl.getAuto(id);
    }
    @Override
    public boolean hasAuto(String id) {
        return impl.hasAuto(id);
    }
    @Override
    public AutoDTO updateAuto(String id, AutoDTO auto) {
        authzHelper.assertUsername(() -> impl.getAuto(id).getUsername());
        return impl.updateAuto(id, auto);
    }
    @Override
    public Page<AutoDTO> queryAutos(AutoDTO probe, Pageable pageable) {
        return impl.queryAutos(probe, pageable);
    }
    @Override
    public Page<AutoDTO> searchAutos(AutoSearchParams searchParams, Pageable pageable) {
        return impl.searchAutos(searchParams, pageable);
    }
    @Override
    public void removeAuto(String id) {
        try {
                String ownername = impl.getAuto(id).getUsername();
                authzHelper.assertRules(()-> authzHelper.isUsername(ownername) || authzHelper.assertMgr(),
                                        username -> String.format("%s is not owner or have MGR role", username));
                impl.removeAuto(id);            
        } catch (ClientErrorException.NotFoundException ex) {
            /* already does not exist -- no error */
        }
    }
    @Override
    public void removeAllAutos() {
        impl.removeAllAutos();
    }
}
