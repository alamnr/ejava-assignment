package info.ejava.assignments.api.autorenters.client.autorentals;

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

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;

public interface AutoRentalsXMLHttpIfaceMapping extends AutoRentalsAPI {

    
    @PostExchange(url = AUTO_RENTALS_PATH,
                    accept = MediaType.APPLICATION_XML_VALUE,
                    contentType = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<AutoRentalDTO> createAutoRental(@RequestBody AutoRentalDTO autoRental);

    
    @GetExchange(url = AUTO_RENTAL_PATH,accept = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<AutoRentalDTO> getAutoRental(@PathVariable("id") String id) ;

    
    @HttpExchange(method = "HEAD", url = AUTO_RENTAL_PATH)
    ResponseEntity<Void> hasAutoRental(@PathVariable("id") String id) ;

    
    @PostExchange(url = AUTO_RENTAL_QUERY_PATH, accept = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<AutoRentalListDTO> queryAutoRental(@RequestBody AutoRentalDTO probe, 
                                                        @RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                                        @RequestParam(name = "pageSize", required = false) Integer pageSize) ;

    @GetExchange(url = AUTO_RENTALS_PATH, accept = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<AutoRentalListDTO> searchAutoRental(
                                @RequestParam(value = "autoId" , required = false) String autoId,
                                @RequestParam(value = "renterId" , required = false) String renterId,
                                @RequestParam(value = "startDate" , required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate,

                                @RequestParam(value = "pageNumber" , required = false) Integer pageNumber,
                                @RequestParam(value = "pageSize", required = false) Integer pageSize);

    @DeleteExchange(url = AUTO_RENTALS_PATH)
    ResponseEntity<Void> removeAllAutoRental() ;

    @DeleteExchange(url = AUTO_RENTAL_PATH)
    ResponseEntity<Void> removeAutoRental(@PathVariable("id") String id) ;
    
    @PutExchange(url = AUTO_RENTAL_PATH,contentType = MediaType.APPLICATION_XML_VALUE, accept = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<AutoRentalDTO> updateAutoRental(@PathVariable("id") String id, @RequestBody AutoRentalDTO autoRental) ;

    
}
