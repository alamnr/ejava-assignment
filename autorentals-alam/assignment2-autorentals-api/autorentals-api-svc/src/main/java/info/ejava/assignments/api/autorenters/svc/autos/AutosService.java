package info.ejava.assignments.api.autorenters.svc.autos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;

public interface AutosService {
    AutoDTO createAuto(AutoDTO  newAuto);
    AutoDTO getAuto(String id);
    boolean hasAuto(String id);
    AutoDTO updateAuto(String id, AutoDTO auto);
    Page<AutoDTO> queryAutos(AutoDTO probe, Pageable pageable);
    Page<AutoDTO> searchAutos(AutoSearchParams searchParams, Pageable pageable);
    void removeAuto(String id);
    void removeAllAutos();
}
