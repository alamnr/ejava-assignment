package info.ejava.assignments.api.autorenters.svc.autorentals;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
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
        
        AutoRentalDTO savedAutoRental = repository.save(newAutoRental);
        log.debug("added autoRental - {}", savedAutoRental);
        return savedAutoRental;
    }

    @Override
    public AutoRentalDTO getAutoRental(String id) {
        return repository.findById(id)
                .orElseThrow(()->{
                    log.debug("getAutoRental({}) not found ", id);
                    throw new ClientErrorException.NotFoundException("autoRental[%s] not found", id);
                });
    }

    @Override
    public boolean hasAutoRental(String id) {
        return repository.existsById(id);
    }

    @Override
    public AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental) {
        validateAutoRental(autoRental);
        if(null == autoRental){
            throw new ClientErrorException.InvalidInputException("autoRental is required");
        }
        if(null !=id && repository.existsById(id)){
            autoRental.setId(id);
        } else {
            throw new ClientErrorException.InvalidInputException("autoRental-[%s] not found", id);
        }
        log.debug("updating auto id - {} , {}",id, autoRental);
        return repository.save(autoRental);
        
    }

    @Override
    public void removeAutoRental(String id) {
        repository.deleteById(id);
    }

    @Override
    public void removeAllAutoRental() {
        repository.deleteAll();
    }

    @Override
    public Page<AutoRentalDTO> queryAutoRental(AutoRentalDTO probe, Pageable pageable) {
         Page<AutoRentalDTO> autoRentals  = repository.findAll(Example.of(probe), pageable);
        if(pageable.isPaged()){
            log.debug("pageNumber-{} / pageSize-{}, returns -{}", pageable.getPageNumber(), pageable.getPageSize(), autoRentals.getNumberOfElements());
        } else {
            log.debug("non page returns - {}", autoRentals.getNumberOfElements());
        }

        return autoRentals;
    }

    @Override
    public Page<AutoRentalDTO> searchAutoRental(RentalSearchParams searchParams, Pageable pageable) {
        Page<AutoRentalDTO> autoRentals = repository.findAllBySearchParam(searchParams, pageable);
        
        if(pageable.isPaged()){
            log.debug("pageNumber -{} / page size - {} / returns -{}", pageable.getPageNumber(), pageable.getPageSize(), autoRentals.getNumberOfElements());
        } else {
            log.debug("non page returns - {}", autoRentals.getNumberOfElements());
        }
        return autoRentals;
    }

    @Override
    public Page<AutoRentalDTO> getAutoRentals(Pageable pageable) {
        Page<AutoRentalDTO> autoRentals = repository.findAll(pageable);
        if(pageable.isPaged()){
            log.debug("pageSize - {}/ pageNumber-{}, returns per page - {}", pageable.getPageSize(),
                            pageable.getPageNumber(), autoRentals.getNumberOfElements());            
        } else {
            log.debug("nonPaged returns total - {}", autoRentals.getNumberOfElements());
        }
        return autoRentals;
    }

    @Override
    public Page<AutoRentalDTO> findByRenterName(String renterName, Pageable pageable) {
       
       return repository.findByRenterName(renterName, pageable);


    }

    private void validateAutoRental(AutoRentalDTO newAutoRental) {
       List<String> errMsg = dtoValidator.validateDto(newAutoRental, null);
       if(!errMsg.isEmpty()){
        throw new ClientErrorException.InvalidInputException("auto rental is not valid - %s", errMsg);
       }
    }
    
}
