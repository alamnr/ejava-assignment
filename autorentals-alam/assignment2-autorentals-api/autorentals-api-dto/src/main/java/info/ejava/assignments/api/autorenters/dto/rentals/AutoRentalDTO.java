package info.ejava.assignments.api.autorenters.dto.rentals;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AutoRentalDTO implements RentalDTO {

    private String remove_me = "don't be empty";

    public AutoRentalDTO(AutoDTO auto, RenterDTO renter, TimePeriod timePeriod){
        
    }
}
