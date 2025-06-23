package info.ejava.assignments.api.autorenters.svc.autos;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AutoServiceImpl implements AutosService {

    private final AutosDTORepository repository;
    private final DtoValidator dtoValidator;

    
    @Override
    public AutoDTO createAuto(AutoDTO newAuto) {
        validateAuto(newAuto);
        
        // if(null == newAuto){
        //     throw new ClientErrorException.InvalidInputException("auto is required");
        // }
        // if(null != newAuto.getId()){
        //     throw new ClientErrorException.InvalidInputException("auto.id must be null");
        // }
        AutoDTO savedAuto = repository.save(newAuto);
        log.debug("added auto - {}", savedAuto);
        return savedAuto;
    }

    @Override
    public AutoDTO getAuto(String id) {
        return repository.findById(id)
                .orElseThrow(()->{
                    log.debug("getAuto({}) not found ", id);
                    throw new ClientErrorException.NotFoundException("auto[%s] not found", id);
                });
    }

    @Override
    public boolean hasAuto(String id) {
        return repository.existsById(id);
    }

    @Override
    public AutoDTO updateAuto(String id, AutoDTO updateAuto) {
        validateAuto(updateAuto);
        
        if(null == updateAuto){
            throw new ClientErrorException.InvalidInputException("auto is required");
        }
        if(null !=id && repository.existsById(id)){
            updateAuto.setId(id);
        } else {
            throw new ClientErrorException.InvalidInputException("auto-[%s] not found", id);
        }
        log.debug("updating auto id - {} , {}",id, updateAuto);
        return repository.save(updateAuto);
    }

    @Override
    public Page<AutoDTO> queryAutos(AutoDTO probe, Pageable pageable) {
        Page<AutoDTO> autos  = repository.findAll(Example.of(probe), pageable);
        if(pageable.isPaged()){
            log.debug("pageNumber-{} / pageSize-{}, returns -{}", pageable.getPageNumber(), pageable.getPageSize(), autos.getNumberOfElements());
        } else {
            log.debug("non page returns - {}", autos.getNumberOfElements());
        }

        return autos;
    }

    @Override
    public Page<AutoDTO> searchAutos(AutoSearchParams searchParams, Pageable pageable) {
        Page<AutoDTO> autos;
        if(searchParams.getMinPassengersInclusive() != null && searchParams.getMaxPassengersInclusive() != null) {
                autos = repository.findByPassengersBetween(searchParams.getMinPassengersInclusive() , searchParams.getMaxPassengersInclusive(), pageable);
        } else if (searchParams.getMinDailyRateInclusive() != null && searchParams.getMaxDailyRateExclusive() != null){
            autos = repository.findByDailyRateBetween(BigDecimal.valueOf(searchParams.getMinDailyRateInclusive()), BigDecimal.valueOf(searchParams.getMaxDailyRateExclusive()), pageable);
        } else {
            autos = repository.findAll(pageable);
        }
        if(pageable.isPaged()){
            log.debug("pageNumber -{} / page size - {} / returns -{}", pageable.getPageNumber(), pageable.getPageSize(), autos.getNumberOfElements());
        } else {
            log.debug("non page returns - {}", autos.getNumberOfElements());
        }
        return autos;

    }

    @Override
    public void removeAuto(String id) {
        repository.deleteById(id);
    }

    @Override
    public void removeAllAutos() {
        repository.deleteAll();
    }

    private void validateAuto(AutoDTO auto) {
       List<String> errMsg = dtoValidator.validateDto(auto, 0);
       if(!errMsg.isEmpty()){
        throw new ClientErrorException.InvalidInputException("auto is not valid - %s", errMsg);
       }
    }
}
