package info.ejava.assignments.api.autorenters.svc.renters;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.POJORepository;

import java.util.Optional;

public interface RentersDTORepository extends POJORepository<RenterDTO> {
    Optional<RenterDTO> findRenterByUsername(String username);
}