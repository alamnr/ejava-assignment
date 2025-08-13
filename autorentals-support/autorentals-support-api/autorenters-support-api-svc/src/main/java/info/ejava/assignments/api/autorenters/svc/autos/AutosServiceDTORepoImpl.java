package info.ejava.assignments.api.autorenters.svc.autos;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
public class AutosServiceDTORepoImpl implements AutosService {
    private final AutosDTORepository repo;

    @Override
    public AutoDTO createAuto(AutoDTO newAuto) {
        if (null==newAuto) {
            throw new ClientErrorException.InvalidInputException("auto is required");
        }
        if (null!=newAuto.getId()) {
            throw new ClientErrorException.InvalidInputException("auto.id must be null");
        }

        AutoDTO savedAuto = repo.save(newAuto);
        log.debug("added auto: {}", savedAuto);
        return savedAuto;
    }

    @Override
    public AutoDTO getAuto(String id) {
        return repo.findById(id)
                .orElseThrow(()->{
                    log.debug("getAuto({}) not found", id);
                    return new ClientErrorException.NotFoundException("Auto[%s] not found", id);
                });
    }

    @Override
    public boolean hasAuto(String id) {
        return repo.existsById(id);
    }

    @Override
    public AutoDTO updateAuto(String id, AutoDTO updateAuto) {
        if (null==updateAuto) {
            throw new ClientErrorException.InvalidInputException("auto is required");
        }
        if (null!=id) {
            updateAuto.setId(id);
        }
        log.debug("updating auto id={}, {}", id, updateAuto);
        return repo.save(updateAuto);
    }

    @Override
    public Page<AutoDTO> queryAutos(AutoDTO probe, Pageable pageable) {
        Page<AutoDTO> autos = repo.findAll(Example.of(probe), pageable);
        if (pageable.isPaged()) {
            log.debug("pageSize {}/pageNumber {}, returns {}", pageable.getPageSize(), pageable.getPageNumber(), autos.getNumberOfElements());
        } else {
            log.debug("nonPaged returns {}", autos.getNumberOfElements());
        }
        return autos;
    }

    @Override
    public Page<AutoDTO> searchAutos(AutoSearchParams searchParams, Pageable pageable) {
        Page<AutoDTO> autos;
        if (searchParams.getMinPassengersInclusive()!=null &&
                searchParams.getMaxPassengersInclusive()!=null) {
            autos = repo.findByPassengersBetween(searchParams.getMinPassengersInclusive(),
                                                searchParams.getMaxPassengersInclusive(),
                                                pageable);
        } else if (searchParams.getMinDailyRateInclusive()!=null &&
                searchParams.getMaxDailyRateExclusive()!=null) {
            autos = repo.findByDailyRateBetween(BigDecimal.valueOf(searchParams.getMinDailyRateInclusive()),
                                                BigDecimal.valueOf(searchParams.getMaxDailyRateExclusive()-1),
                                                pageable);
        } else {
            autos = repo.findAll(pageable);
        }

        if (pageable.isPaged()) {
            log.debug("pageSize {}/pageNumber {}, returns {}", pageable.getPageSize(), pageable.getPageNumber(), autos.getNumberOfElements());
        } else {
            log.debug("nonPaged returns {}", autos.getNumberOfElements());
        }
        return autos;
    }

    @Override
    public void removeAuto(String id) {
        repo.deleteById(id);
    }

    @Override
    public void removeAllAutos() {
        repo.deleteAll();
    }
}
