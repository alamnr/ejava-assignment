package info.ejava.assignments.security.autorenters.svc.renters;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NoSecurityRentersServiceWrapper implements RentersService {

    private final RentersService impl;

    @Override
    public RenterDTO createRenter(RenterDTO newrenter) {
        newrenter.setUsername("anonymous");
        return impl.createRenter(newrenter);
    }

    @Override
    public RenterDTO getRenter(String id) {
        return impl.getRenter(id);
    }

    @Override
    public boolean hasRenter(String id) {
        return impl.hasRenter(id);
    }

    @Override
    public RenterDTO updateRenter(String id, RenterDTO renterDTO) {
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

    @Override
    public void removeRenter(String id) {
        impl.removeRenter(id);
    }

    @Override
    public void removeAllRenters() {
        impl.removeAllRenters();
    }
}
