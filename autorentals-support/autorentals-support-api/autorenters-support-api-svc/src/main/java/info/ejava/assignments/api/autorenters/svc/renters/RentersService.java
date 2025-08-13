package info.ejava.assignments.api.autorenters.svc.renters;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RentersService {
    RenterDTO createRenter(RenterDTO newRenter);
    RenterDTO getRenter(String id);
    boolean hasRenter(String id);
    RenterDTO updateRenter(String id, RenterDTO renterDTO);
    Page<RenterDTO> getRenters(Pageable pageable);
    Optional<RenterDTO> findRenterByUsername(String username);
    void removeRenter(String id);
    void removeAllRenters();
}
