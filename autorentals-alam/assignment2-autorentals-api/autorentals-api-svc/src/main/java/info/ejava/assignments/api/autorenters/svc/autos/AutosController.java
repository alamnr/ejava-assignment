package info.ejava.assignments.api.autorenters.svc.autos;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.examples.common.exceptions.ClientErrorException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AutosController {
    private final AutosService autosService;

    @PostConstruct
    public void init(){
        log.info("Autos initialized , URI - {}", AutosAPI.AUTOS_PATH);
    }

    @RequestMapping(path = AutosAPI.AUTOS_PATH, method = RequestMethod.POST,
                            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<AutoDTO> createAuto(@RequestBody AutoDTO auto){
        AutoDTO addedAuto = autosService.createAuto(auto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(AutosAPI.AUTO_PATH).build(addedAuto.getId()); 
        ResponseEntity<AutoDTO> response = ResponseEntity.created(location).body(addedAuto); // http status code - 201
        return response;
    }

    /*
     * This query end point uses an example probe to match the non null properties exactly
     * @param pageNumber / offset
     * @param pageSize / limit
     * @param probe an AutoDTO object containing property value to match exactly against 
     * null values are ignored
     * @return list of contents containing autos
     */

     @RequestMapping(path = AutosAPI.AUTOS_QUERY_PATH,method = RequestMethod.POST, 
                        produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
     public ResponseEntity<AutoListDTO> queryAutosList(@RequestParam(value = "pageNumber", required = false)Integer pageNumber, 
                    @RequestParam(value = "pageSize", required = false)Integer pageSize, @RequestBody AutoDTO probe) {

        Pageable pageable = (null != pageNumber && null != pageSize && pageNumber>0 && pageSize>0) ? 
                            PageRequest.of(pageNumber, pageSize) : Pageable.unpaged();
        Page<AutoDTO> autosPage = autosService.queryAutos(probe, pageable);
        AutoListDTO autosList = new AutoListDTO(pageNumber==null?0:pageNumber,
                                                pageSize==null?0:pageSize,autosPage.getContent().size(),
                                                "",autosPage.toList());
        ResponseEntity<AutoListDTO> response = ResponseEntity.ok(autosList); // http status code - 200
        return response;

     }


     /*
      * These query end point uses non-axact search criteria to form range queries
      * Most of the values are inclusive. One is exclusive to just provide an example
      * of its potential impact. Not all properties are represented
      * @param minDailyRate inclusive
      * @param maxDailyRate exclusive
      * @param minPassengers inclusive
      * @param maxPassengers inclusive
      * @param pageNumber window of autos based on the pageSize; all if not supplied
      * @param pageSize number of autos to include in response; all if not supplied
      * @return list of contents containing autos

      */

     @RequestMapping(path = AutosAPI.AUTOS_PATH,method = RequestMethod.GET,
                            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})

      public ResponseEntity<AutoListDTO>  searchAutosList(
            @RequestParam(value = "minDailyRate", required = false)Integer minDailyRate,
            @RequestParam(value = "maxDailyRate", required=false) Integer maxDailyRate,
            @RequestParam(value = "minPassengers", required=false) Integer minPassengers,
            @RequestParam(value = "maxPassengers", required = false) Integer maxPassengers,

            @RequestParam(value = "pageNumber" , required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize){

                Pageable pageable = ( null != pageNumber && null != pageSize && pageNumber>=0 && pageSize>0) ? 
                                    PageRequest.of(pageNumber, pageSize) : Pageable.unpaged();
                AutoSearchParams searchParams = AutoSearchParams.builder()
                                                .minDailyRateInclusive(minDailyRate)
                                                .maxDailyRateExclusive(maxDailyRate)
                                                .minPassengersInclusive(minPassengers)
                                                .maxPassengersInclusive(maxPassengers)
                                                .build();
                Page<AutoDTO> autosPage = autosService.searchAutos(searchParams, pageable);
                AutoListDTO autosList = new AutoListDTO(pageNumber==null?0:pageNumber,pageSize==null?0:pageSize,
                                                        autosPage.getContent().size() ,"",autosPage.toList());
                ResponseEntity<AutoListDTO> response = ResponseEntity.ok(autosList); // http status code - 200
                return response;

      }

      
      @RequestMapping(path = AutosAPI.AUTO_PATH, method = RequestMethod.GET,
                        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
      public ResponseEntity<AutoDTO> getAuto(@PathVariable("id")String id) {
        AutoDTO auto = autosService.getAuto(id);
            ResponseEntity<AutoDTO> response = ResponseEntity.ok(auto);
        return response;
      }

      @RequestMapping(path = AutosAPI.AUTO_PATH,  method = RequestMethod.HEAD,
                            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
      public ResponseEntity<Void> hasAuto(@PathVariable("id")String id) {
        boolean exist = autosService.hasAuto(id);
        if(!exist){
            throw new ClientErrorException.NotFoundException("auto[%s] does not exist.", id);
        }

        ResponseEntity<Void> response = ResponseEntity.ok().build();  // http status code  - 200
        return response;
      }

    @RequestMapping(path = AutosAPI.AUTO_PATH, method = RequestMethod.PUT,
                    consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<AutoDTO> updateAuto(@PathVariable("id") String id, @RequestBody AutoDTO auto){

        AutoDTO updatedAuto = autosService.updateAuto(id, auto);
        ResponseEntity<AutoDTO> response = ResponseEntity.ok(updatedAuto); // http status code - 200
        return response;
    }

    @RequestMapping(path = AutosAPI.AUTO_PATH, method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeAuto(@PathVariable("id") String id) {
        autosService.removeAuto(id);
        return ResponseEntity.noContent().build();  // http status code  - 204
    }


    @RequestMapping(path = AutosAPI.AUTOS_PATH, method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeAllAutos(){
        autosService.removeAllAutos();
        return ResponseEntity.noContent().build();   // http status code - 204
    }

}
