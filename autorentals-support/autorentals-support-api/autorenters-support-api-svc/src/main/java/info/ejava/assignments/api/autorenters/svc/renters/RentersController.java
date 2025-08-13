package info.ejava.assignments.api.autorenters.svc.renters;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.net.URI;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RentersController {
    private final RentersService rentersService;

    @PostConstruct
    public void init() {
        log.info("Renters initialized, URI={}", RentersAPI.RENTERS_PATH);
    }

    @PostMapping(path = RentersAPI.RENTERS_PATH,
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RenterDTO> createRenter(@RequestBody RenterDTO newRenter) {
        RenterDTO addedRenter = rentersService.createRenter(newRenter);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath(RentersAPI.RENTER_PATH)
                .build(addedRenter.getId());
        ResponseEntity<RenterDTO> response = ResponseEntity.created(location).body(addedRenter);
        return response;
    }

    @GetMapping(path = RentersAPI.RENTERS_PATH,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RenterListDTO> getRentersList(
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Pageable pageable = (null!=pageNumber && null!=pageSize && pageNumber >= 0 && pageSize > 0) ?
                PageRequest.of(pageNumber, pageSize) : Pageable.unpaged();
        Page<RenterDTO> rentersPage = rentersService.getRenters(pageable);

        RenterListDTO rentersList = new RenterListDTO(rentersPage.getContent());
        ResponseEntity<RenterListDTO> response = ResponseEntity.ok(rentersList);
        return response;
    }

    @GetMapping(path = RentersAPI.RENTER_PATH,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RenterDTO> getRenter(@PathVariable("id") String id) {
        RenterDTO foundRenter = rentersService.getRenter(id);

        ResponseEntity<RenterDTO> response = ResponseEntity.ok(foundRenter);
        return response;
    }

    @RequestMapping(path = RentersAPI.RENTER_PATH,
            method = RequestMethod.HEAD)
    public ResponseEntity<Void> hasRenter(@PathVariable("id") String id) {
        if (!rentersService.hasRenter(id)) {
            throw new ClientErrorException.NotFoundException("Renter[%s] not found",id);
        }

        ResponseEntity<Void> response = ResponseEntity.ok().build();
        return response;
    }

    @PutMapping(path = RentersAPI.RENTER_PATH,
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RenterDTO> updateRenter(
            @PathVariable("id") String id,
            @RequestBody RenterDTO renter) {
        RenterDTO updatedRenter = rentersService.updateRenter(id, renter);

        ResponseEntity<RenterDTO> response = ResponseEntity.ok(updatedRenter);
        return response;
    }

    @DeleteMapping(path = RentersAPI.RENTER_PATH)
    public ResponseEntity<Void> removeRenter(@PathVariable String id) {
        rentersService.removeRenter(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = RentersAPI.RENTERS_PATH)
    public ResponseEntity<Void> removeAllRenters() {
        rentersService.removeAllRenters();
        return ResponseEntity.noContent().build();
    }
}
