package info.ejava.assignments.api.autorenters.svc.renters;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.exceptions.ClientErrorException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RentersController {

    private final RenterService renterService;

    @PostConstruct
    public void init() {
        log.info("Renters initialized, URI={} ",RentersAPI.RENTERS_PATH);
    }

    @PostMapping(path = RentersAPI.RENTERS_PATH,
                consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE},
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RenterDTO> createRenter(@RequestBody  RenterDTO newRenter) {
        RenterDTO addedRenter = renterService.createRenter(newRenter);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                        .replacePath(RentersAPI.RENTER_PATH).build(addedRenter.getId());
        ResponseEntity<RenterDTO> response = ResponseEntity.created(location).body(addedRenter);
        return response;
    }

    @GetMapping(path = RentersAPI.RENTERS_PATH,
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RenterListDTO> getRentersList(
            @RequestParam(value = "pageNumber" , defaultValue = "0")Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "0") Integer pageSize ) {

            Pageable pageable = (null != pageNumber && null != pageSize && pageNumber>=0 && pageSize>0) ?
                                    PageRequest.of(pageNumber,pageSize) :Pageable.unpaged();
            Page<RenterDTO> rentersPage = renterService.getRenters(pageable);
            //log.info("renterPage- {}, pageNumber -{}, pageSize- {}", rentersPage, pageNumber, pageSize);
            RenterListDTO rentersList = new RenterListDTO(pageNumber,pageSize,
                                        rentersPage.getContent().size(),"",rentersPage.getContent());
            
             
            ResponseEntity<RenterListDTO> response = ResponseEntity.ok().body(rentersList);
            return response;
    }

    @GetMapping(path = RentersAPI.RENTER_PATH,
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RenterDTO> getRenter(@PathVariable("id") String id) {
        RenterDTO foundRenter = renterService.getRenter(id);
        ResponseEntity<RenterDTO> response = ResponseEntity.ok(foundRenter);
        return response;
    }


    @RequestMapping(path = RentersAPI.RENTER_PATH,
                    method = RequestMethod.HEAD)
    public ResponseEntity<Void> hasRenter(@PathVariable("id")String id) {
        if(!renterService.hasRenter(id)){
            throw new ClientErrorException.NotFoundException("Renter [%s] not found", id);
        }

        ResponseEntity<Void> response = ResponseEntity.ok().build();
        return response;
    }

    @PutMapping(path = RentersAPI.RENTER_PATH,consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RenterDTO> updateRenter(@PathVariable("id")String id, @RequestBody RenterDTO renter){
        RenterDTO updatedRenter = renterService.updateRenter(id, renter);
        ResponseEntity<RenterDTO> response = ResponseEntity.ok(updatedRenter);
        return response;

    }

    @DeleteMapping(path = RentersAPI.RENTER_PATH)
    public ResponseEntity<Void> removeRenter(@PathVariable("id")String id) {
        renterService.removeRenter(id);
        return ResponseEntity.noContent().build();
    }

    
    @DeleteMapping(path = RentersAPI.RENTERS_PATH)
    public ResponseEntity<Void> removeAllRenters(){
        renterService.removeAllRenters();
        return ResponseEntity.noContent().build();
    }

    // Idempotence is a characteristics where a repeated event produces same result each time executed
    // Idempotent http  method - GET, PUT, DELETE, HEAD etc  . For idempotent http method  browser page is automatically refresh, 
    // no warning dialogue is shown
    // For idempotent http method - POST, browser page is not automatically refres, a warning dialogue is shown each time

}
