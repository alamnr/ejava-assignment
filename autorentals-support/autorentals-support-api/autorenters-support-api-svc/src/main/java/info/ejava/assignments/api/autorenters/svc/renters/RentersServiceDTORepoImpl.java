package info.ejava.assignments.api.autorenters.svc.renters;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RentersServiceDTORepoImpl implements RentersService {
    private final RentersDTORepository repo;

    @Override
    public RenterDTO createRenter(RenterDTO newRenter) {
        if (null== newRenter) {
            throw new ClientErrorException.InvalidInputException("renter is required");
        }
        if (null!= newRenter.getId()) {
            throw new ClientErrorException.InvalidInputException("renter.id must be null");
        }

        RenterDTO savedRenter = repo.save(newRenter);
        log.debug("added renter: {}", savedRenter);
        return savedRenter;
    }

    @Override
    public RenterDTO getRenter(String id) {
        return repo.findById(id)
                .orElseThrow(()->{
                    log.debug("getRenter({}) not found", id);
                    throw new ClientErrorException.NotFoundException("Renter[%s] not found", id);
                });
    }

    @Override
    public boolean hasRenter(String id) {
        return repo.existsById(id);
    }

    @Override
    public RenterDTO updateRenter(String id, RenterDTO updateRenter) {
        if (null==updateRenter) {
            throw new ClientErrorException.InvalidInputException("renter is required");
        }
        if (null!=id) {
            updateRenter.setId(id);
        }
        log.debug("updating renter id={}, {}", id, updateRenter);
        return repo.save(updateRenter);
    }

    @Override
    public Page<RenterDTO> getRenters(Pageable pageable) {
        Page<RenterDTO> renters = repo.findAll(pageable);
        if (pageable.isPaged()) {
            log.debug("pageSize {}/pageNumber {}, returns {}", pageable.getPageSize(), pageable.getPageNumber(), renters.getNumberOfElements());
        } else {
            log.debug("nonPaged returns {}", renters.getNumberOfElements());
        }
        return renters;
    }

    @Override
    public Optional<RenterDTO> findRenterByUsername(String username) {
        return repo.findRenterByUsername(username);
    }

    @Override
    public void removeRenter(String id) {
        repo.deleteById(id);
    }

    @Override
    public void removeAllRenters() {
        repo.deleteAll();
    }
}
