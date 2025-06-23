package info.ejava.assignments.api.autorenters.svc.autorentals;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.SearchParams;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AutoRentalServiceImpl  implements AutoRentalService {

    private final AutoRentalDTORepository repository;
    private final DtoValidator dtoValidator;

    @Override
    public AutoRentalDTO createAutoRental(AutoRentalDTO newAutoRental) {
        validateAutoRental(newAutoRental);
         // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAutoRental'");
    }

    @Override
    public AutoRentalDTO getAutoRental(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAutoRental'");
    }

    @Override
    public boolean hasAutoRental(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAutoRental'");
    }

    @Override
    public AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental) {
        validateAutoRental(autoRental);
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAutoRental'");
    }

    @Override
    public void removeAutoRental(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAutoRental'");
    }

    @Override
    public void removeAllAutoRental() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAllAutoRental'");
    }

    @Override
    public Page<AutoRentalDTO> queryAutoRental(AutoRentalDTO probe, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'queryAutoRental'");
    }

    @Override
    public Page<AutoRentalDTO> searchAutoRental(SearchParams searchParams, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchAutoRental'");
    }

    @Override
    public Page<AutoRentalDTO> getAutoRentals(Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAutoRentals'");
    }

    @Override
    public Optional<AutoRentalDTO> findAutoRentalsByRenterName(String renterName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAutoRentalsByRenterName'");
    }

    private void validateAutoRental(AutoRentalDTO newAutoRental) {
       List<String> errMsg = dtoValidator.validateDto(newAutoRental, null);
       if(!errMsg.isEmpty()){
        throw new ClientErrorException.InvalidInputException("auto rental is not valid - %s", errMsg);
       }
    }
    
}
