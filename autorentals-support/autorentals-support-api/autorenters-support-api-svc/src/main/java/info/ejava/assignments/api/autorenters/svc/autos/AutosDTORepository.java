package info.ejava.assignments.api.autorenters.svc.autos;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface AutosDTORepository {
    AutoDTO save(AutoDTO auto);
    Optional<AutoDTO> findById(String id);
    boolean existsById(String id);
    Page<AutoDTO> findAll(Pageable pageable);
    long count();
    void deleteById(String id);
    void deleteAll();

    Page<AutoDTO> findAll(Example<AutoDTO> example, Pageable pageable);
    Page<AutoDTO> findByPassengersBetween(int minPassengers, int maxPassengers, Pageable pageable);
    Page<AutoDTO> findByDailyRateBetween(BigDecimal minDailyRate, BigDecimal maxDailyRate, Pageable pageable);
}