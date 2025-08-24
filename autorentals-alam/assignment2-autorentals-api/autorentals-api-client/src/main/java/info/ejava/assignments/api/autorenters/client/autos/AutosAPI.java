package info.ejava.assignments.api.autorenters.client.autos;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/**
 * This interface defines the REST-like, RMM-level3 interface to the
 * Auto Service.
 */
public interface AutosAPI {
    String AUTOS_PATH = "/api/autos";
    String AUTO_PATH = "/api/autos/{id}";
    String AUTOS_QUERY_PATH = "/api/autos/query";

    AutosAPI withRestTemplate(RestTemplate restTemplate);

    ResponseEntity<AutoDTO> createAuto(AutoDTO auto);
    ResponseEntity<AutoListDTO> queryAutos(AutoDTO probe, Integer pageNumber, Integer pageSize);
    // ResponseEntity<AutoListDTO> searchAutos(Integer minDailyRate,Integer maxDailyRate,
    //             Integer minPassengers,Integer maxPassengers,Integer pageNumber,Integer pageSize);
    ResponseEntity<AutoListDTO> searchAutos(AutoSearchParams searchParams);
    ResponseEntity<AutoDTO> getAuto(String id);
    ResponseEntity<Void> hasAuto(String id);
    ResponseEntity<AutoDTO> updateAuto(String id, AutoDTO auto);
    ResponseEntity<Void> removeAuto(String id);
    ResponseEntity<Void> removeAllAutos();
}
