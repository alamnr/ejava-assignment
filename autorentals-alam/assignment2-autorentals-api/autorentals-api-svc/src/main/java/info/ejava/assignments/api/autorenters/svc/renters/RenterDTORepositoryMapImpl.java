package info.ejava.assignments.api.autorenters.svc.renters;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.POJORepositoryMapImpl;


public class RenterDTORepositoryMapImpl extends POJORepositoryMapImpl<RenterDTO>
        implements RenterDTORepository {
    public RenterDTORepositoryMapImpl(){
        super(renter->renter.getId(),(renter,id)->renter.setId(id),"renter-" );
    }
    @Override
    public Optional<RenterDTO> findRenterByUserName(String username) {
        return super.findFirst(renter-> StringUtils.equals(renter.getUsername(), username));
    }
}
