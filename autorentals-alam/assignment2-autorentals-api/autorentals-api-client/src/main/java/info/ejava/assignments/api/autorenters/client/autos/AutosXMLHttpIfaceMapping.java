package info.ejava.assignments.api.autorenters.client.autos;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;

public interface AutosXMLHttpIfaceMapping extends AutosAPI {

    @Override
    @PostExchange(url = AUTOS_PATH,
                    accept = MediaType.APPLICATION_XML_VALUE,
                    contentType = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<AutoDTO> createAuto(@RequestBody AutoDTO auto) ;

    @Override
    @GetExchange(url = AUTO_PATH,accept = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<AutoDTO> getAuto(@PathVariable("id") String id);

    @Override
    @HttpExchange(method = "HEAD", url = AUTO_PATH)
    public ResponseEntity<Void> hasAuto(@PathVariable("id") String id) ;

    @Override
    @PostExchange(url = AUTOS_QUERY_PATH, accept = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<AutoListDTO> queryAutos(@RequestBody AutoDTO probe, 
                        @RequestParam(name = "pageNumber", required = false) Integer pageNumber, 
                        @RequestParam(name = "pageSize", required = false) Integer pageSize) ;

    
    @GetExchange(url = AUTOS_PATH, accept = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<AutoListDTO> searchAutosList(
            @RequestParam(value = "minDailyRate", required = false)Integer minDailyRate,
            @RequestParam(value = "maxDailyRate", required=false) Integer maxDailyRate,
            @RequestParam(value = "minPassengers", required=false) Integer minPassengers,
            @RequestParam(value = "maxPassengers", required = false) Integer maxPassengers,

            @RequestParam(value = "pageNumber" , required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize);
    


    @Override
    @DeleteExchange(url = AUTOS_PATH)
    public ResponseEntity<Void> removeAllAutos() ;

    @Override
    @DeleteExchange(url = AUTO_PATH)
    public ResponseEntity<Void> removeAuto(@PathVariable("id") String id) ;

    

    @Override
    @PutExchange(url = AUTO_PATH,contentType = MediaType.APPLICATION_XML_VALUE, accept = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<AutoDTO> updateAuto(@PathVariable("id") String id, @RequestBody AutoDTO auto);
}
