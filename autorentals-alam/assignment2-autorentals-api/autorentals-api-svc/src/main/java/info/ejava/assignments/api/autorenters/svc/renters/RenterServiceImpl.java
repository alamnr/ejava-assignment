package info.ejava.assignments.api.autorenters.svc.renters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.utils.RenterValidator;
import info.ejava.assignments.api.autorenters.svc.utils.RentersProperties;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RenterServiceImpl implements RenterService {

    private final RenterDTORepository repo;
    private final RenterValidator renterValidator;
    private final RentersProperties renterProps;

    @Override
    public RenterDTO createRenter(RenterDTO newRenter){
        /*
        if(null==newRenter){
            throw new ClientErrorException.InvalidInputException("renter is required - [%s]", newRenter);

        }
        if(null != newRenter.getId()){
            throw new ClientErrorException.InvalidInputException("renter.id can not be null - [id] [%s]", newRenter.getId());
        } */
        validateRenter(newRenter);
        RenterDTO savedRenter = repo.save(newRenter);
        log.debug("added renter: {}", savedRenter);
        return savedRenter;
    }

    private void validateRenter(RenterDTO newRenter) {
       List<String> errMsg = renterValidator.validateNewRenter(newRenter, renterProps.getMinAge());
       if(!errMsg.isEmpty()){
        throw new ClientErrorException.InvalidInputException("renter is not valid - %s", errMsg);
       }
    }

    @Override
    public RenterDTO getRenter(String id) {
        return repo.findById(id)
                .orElseThrow(()-> {
                    log.debug("getRenter- {} , not found", id);
                    throw new ClientErrorException.NotFoundException("Renter-[%s] not found", id);
                });
    }

    @Override
    public boolean hasRenter(String id) {
        return repo.existsById(id);
    }

    @Override
    public RenterDTO updateRenter(String id, RenterDTO updateRenter) {
        validateRenter(updateRenter);
        if(null == updateRenter){
            throw new ClientErrorException.InvalidInputException("renter is required", null);
        }

        if(null != id && repo.existsById(id)){
            updateRenter.setId(id);
        }
        else{
            throw new ClientErrorException.InvalidInputException("renter-[%s] not found", id);
        }
        log.debug("updating renter havind id - {}, renter - {}", id, updateRenter);
        return repo.save(updateRenter);
    }

    @Override
    public Page<RenterDTO> getRenters(Pageable pageable) {
        Page<RenterDTO> renters = repo.findAll(pageable);
        if(pageable.isPaged()){
            log.debug("pageSize - {}/ pageNumber-{}, returns per page - {}", pageable.getPageSize(),
                            pageable.getPageNumber(), renters.getNumberOfElements());            
        } else {
            log.debug("nonPaged returns total - {}", renters.getNumberOfElements());
        }
        return renters;
    }

    @Override
    public Optional<RenterDTO> findRenterByUsername(String username) {
        return repo.findRenterByUserName(username);
    }

    @Override
    public void removeRenter(String id) {
        repo.deleteById(id);
    }

    @Override
    public void removeAllRenters(){
        repo.deleteAll();
    }
} 
