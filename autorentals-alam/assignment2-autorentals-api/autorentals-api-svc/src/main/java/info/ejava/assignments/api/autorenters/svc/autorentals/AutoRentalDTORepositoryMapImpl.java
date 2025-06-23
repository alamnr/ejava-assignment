package info.ejava.assignments.api.autorenters.svc.autorentals;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.svc.POJORepositoryMapImpl;

public class AutoRentalDTORepositoryMapImpl  extends POJORepositoryMapImpl<AutoRentalDTO>
    implements AutoRentalDTORepository {

    public AutoRentalDTORepositoryMapImpl() {
        super(autoRenter -> autoRenter.getId(), (autoRenter,id)->autoRenter.setId(id), "autoRental-");
    }
     
}
