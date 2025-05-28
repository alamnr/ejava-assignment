package info.ejava.assignments.api.autorenters.svc.renters;

import java.util.Optional;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.POJORepository;

public interface RenterDTORepository extends POJORepository<RenterDTO> {
    Optional<RenterDTO> findRenterByUserName(String username);
}
