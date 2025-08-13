package info.ejava.assignments.api.autorenters.svc.renters;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.POJORepositoryMapImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class RentersDTORepositoryMapImpl
        extends POJORepositoryMapImpl<RenterDTO>
        implements RentersDTORepository {
    public RentersDTORepositoryMapImpl() {
        super(RenterDTO::getId, RenterDTO::setId, "renter-"); //supply PK getter and setter
    }

    @Override
    public Optional<RenterDTO> findRenterByUsername(String username) {
        return super.findFirst(renter-> StringUtils.equals(renter.getUsername(), username));
    }
}
