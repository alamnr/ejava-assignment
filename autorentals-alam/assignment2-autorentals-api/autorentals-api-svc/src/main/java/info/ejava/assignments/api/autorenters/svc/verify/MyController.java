package info.ejava.assignments.api.autorenters.svc.verify;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.svc.renters.RenterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
//@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class MyController {

    private final RenterService renterService;

    @PostConstruct
    public void init() {
        log.info("Renters initialized, URI={} ",RentersAPI.RENTERS_PATH);
    }

    
    @GetMapping(path = "/api/hello", 
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }
}
