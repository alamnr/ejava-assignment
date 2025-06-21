package info.ejava.assignments.api.autorenters.svc.autorentals;

import java.net.URI;
import java.time.LocalDate;

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

import info.ejava.assignments.api.autorenters.client.autorentals.AutoRentalsAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.SearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.examples.common.exceptions.ClientErrorException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AutoRentalController {
    
    private final AutoRentalService autoRentalService;

    @PostConstruct
    public void init(){
        log.info("AutoRental initialized , URI - {}", AutoRentalsAPI.AUTO_RENTALS_PATH);
    }

    /*
     * Http Method - POST for create - on success , returned HTTP Status - CREATED, code - 201 
     * Http Method - GET, PUT, HEAD, POST for search -  on success , returned HTTP Status - OK , code - 200
     * Http Method - DELETE for search - on success , returned HTTP Status - NO CONTENT , Status code - 204
     */

    @RequestMapping(path = AutoRentalsAPI.AUTO_RENTALS_PATH, method = RequestMethod.POST,
                            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<AutoRentalDTO> createAutoRental(@RequestBody AutoRentalDTO autoRental){
        AutoRentalDTO addedAutoRental = autoRentalService.createAutoRental(autoRental);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                            .replacePath(AutoRentalsAPI.AUTO_RENTAL_PATH)
                            .build(addedAutoRental.getId()); 
        ResponseEntity<AutoRentalDTO> response = ResponseEntity.created(location).body(addedAutoRental); // http status code - 201
        return response;
    }

    /*
     * This query end point uses an example probe to match the non null properties exactly
     * @param pageNumber / offset
     * @param pageSize / limit
     * @param probe an AutoRentalDTO object containing property value to match exactly against 
     * null values are ignored
     * @return list of contents containing autos
     */

     @RequestMapping(path = AutoRentalsAPI.AUTO_RENTAL_QUERY_PATH,method = RequestMethod.POST, 
                        produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
     public ResponseEntity<AutoRentalListDTO> queryAutoRentalList(@RequestParam(value = "pageNumber", required = false)Integer pageNumber, 
                    @RequestParam(value = "pageSize", required = false)Integer pageSize, @RequestBody AutoDTO probe) {

        Pageable pageable = (null != pageNumber && null != pageSize && pageNumber>0 && pageSize>0) ? 
                            PageRequest.of(pageNumber, pageSize) : Pageable.unpaged();
        Page<AutoRentalDTO> autoRentalPage = autoRentalService.queryAutoRental(probe, pageable);
        AutoRentalListDTO autoRentalList = new AutoRentalListDTO(pageNumber==null?0:pageNumber,
                                                pageSize==null?0:pageSize,autoRentalPage.getContent().size(),
                                                "",autoRentalPage.toList());
        ResponseEntity<AutoRentalListDTO> response = ResponseEntity.ok(autoRentalList); // http status code - 200
        return response;

     }


     /*
      * These query end point uses non-axact search criteria to form range queries
      * Most of the values are inclusive. One is exclusive to just provide an example
      * of its potential impact. Not all properties are represented
      * @param autoId
      * @param renterId
      * @param startDate
      * @param endDate
      * @param pageNumber window of autos based on the pageSize; all if not supplied
      * @param pageSize number of autos to include in response; all if not supplied
      * @return list of contents containing autos

      */

     @RequestMapping(path = AutoRentalsAPI.AUTO_RENTALS_PATH,method = RequestMethod.GET,
                            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})

      public ResponseEntity<AutoRentalListDTO>  searchAutoRentalList(
            @RequestParam(value = "autoId" , required = false) String autoId,
            @RequestParam(value = "renterId" , required = false) String renterId,
            @RequestParam(value = "startDate" , required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,

            @RequestParam(value = "pageNumber" , required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize){

                Pageable pageable = ( null != pageNumber && null != pageSize && pageNumber>=0 && pageSize>0) ? 
                                    PageRequest.of(pageNumber, pageSize) : Pageable.unpaged();
                
                TimePeriod timePeriod = TimePeriod.builder().startDate(LocalDate.parse(startDate)).endDate(LocalDate.parse(endDate)).build();
                SearchParams searchParams = SearchParams.builder()
                                                .autoId(autoId)
                                                .renterId(renterId)
                                                .timePeriod(timePeriod)
                                                .build();
                Page<AutoRentalDTO> autoRentalPage = autoRentalService.searchAutoRental(searchParams, pageable);
                AutoRentalListDTO autoRentalList = new AutoRentalListDTO(pageNumber==null?0:pageNumber,pageSize==null?0:pageSize,
                                                        autoRentalPage.getContent().size() ,"",autoRentalPage.toList());
                ResponseEntity<AutoRentalListDTO> response = ResponseEntity.ok(autoRentalList); // http status code - 200
                return response;

      }

      
      @RequestMapping(path = AutoRentalsAPI.AUTO_RENTAL_PATH, method = RequestMethod.GET,
                        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
      public ResponseEntity<AutoRentalDTO> getAutoRental(@PathVariable("id")String id) {
        AutoRentalDTO autoRental = autoRentalService.getAutoRental(id);
            ResponseEntity<AutoRentalDTO> response = ResponseEntity.ok(autoRental);
        return response;
      }

      @RequestMapping(path = AutoRentalsAPI.AUTO_RENTAL_PATH,  method = RequestMethod.HEAD,
                            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
      public ResponseEntity<Void> hasAutoRental(@PathVariable("id")String id) {
        boolean exist = autoRentalService.hasAutoRental(id);
        if(!exist){
            throw new ClientErrorException.NotFoundException("autoRental[%s] does not exist.", id);
        }

        ResponseEntity<Void> response = ResponseEntity.ok().build();  // http status code  - 200
        return response;
      }

    @RequestMapping(path = AutoRentalsAPI.AUTO_RENTAL_PATH, method = RequestMethod.PUT,
                    consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<AutoRentalDTO> updateAutoRental(@PathVariable("id") String id, @RequestBody AutoRentalDTO auto){

        AutoRentalDTO updatedAutoRental = autoRentalService.updateAutoRental(id, auto);
        ResponseEntity<AutoRentalDTO> response = ResponseEntity.ok(updatedAutoRental); // http status code - 200
        return response;
    }

    @RequestMapping(path = AutoRentalsAPI.AUTO_RENTAL_PATH, method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeAutoRental(@PathVariable("id") String id) {
        autoRentalService.removeAuto(id);
        return ResponseEntity.noContent().build();  // http status code  - 204
    }


    @RequestMapping(path = AutoRentalsAPI.AUTO_RENTALS_PATH, method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeAllAutoRental(){
        autoRentalService.removeAllAutos();
        return ResponseEntity.noContent().build();   // http status code - 204
    }

}
