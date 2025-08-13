package info.ejava.assignments.security.autorenters.svc.autos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NoSecurityAutosServiceWrapper implements AutosService {
    
    private final AutosService impl;

    @Override
    public AutoDTO createAuto(AutoDTO newAuto) {
        newAuto.setUsername("anonymous");
        return impl.createAuto(newAuto);
    }

    @Override
    public AutoDTO getAuto(String id) {
        return impl.getAuto(id);
    }

    @Override
    public boolean hasAuto(String id) {
        return impl.hasAuto(id);
    }

    @Override
    public AutoDTO updateAuto(String id, AutoDTO auto) {
        return impl.updateAuto(id, auto);
    }

    @Override
    public Page<AutoDTO> queryAutos(AutoDTO probe, Pageable pageable) {
        return impl.queryAutos(probe, pageable);
    }

    @Override
    public Page<AutoDTO> searchAutos(AutoSearchParams searchParams, Pageable pageable) {
        return impl.searchAutos(searchParams, pageable);
    }

    @Override
    public void removeAuto(String id) {
        impl.removeAuto(id);
    }

    @Override
    public void removeAllAutos() {
        impl.removeAllAutos();
    }


}
