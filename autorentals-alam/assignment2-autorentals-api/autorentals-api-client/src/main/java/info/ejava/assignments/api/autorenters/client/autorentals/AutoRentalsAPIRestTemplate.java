package info.ejava.assignments.api.autorenters.client.autorentals;


import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.examples.common.web.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Setter
@Getter
public class AutoRentalsAPIRestTemplate implements AutoRentalsAPI {
    protected URI baseUrl;
    protected RestTemplate restTemplate;
    protected MediaType mediaType;

    public AutoRentalsAPIRestTemplate(RestTemplate restTemplate, ServerConfig serverConfig, MediaType mediaType) {
        this.baseUrl = serverConfig.getBaseUrl();
        this.restTemplate = restTemplate;
        this.mediaType = mediaType;
    }
    
    public AutoRentalsAPIRestTemplate withRestTemplate(RestTemplate restTemplate) {
        ServerConfig serverConfig = new ServerConfig().withBaseUrl(baseUrl).build();
        return new AutoRentalsAPIRestTemplate(restTemplate, serverConfig, mediaType);
    }

    @Override
    public ResponseEntity<AutoRentalDTO> createAutoRental(AutoRentalDTO autoRental) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTALS_PATH).build().toUri();
        
        RequestEntity<AutoRentalDTO> request = RequestEntity.post(url)
                                        .accept(mediaType)
                                        .contentType(mediaType)
                                        .body(autoRental);
        ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(request, AutoRentalDTO.class);

        return response;
    }

    @Override
    public ResponseEntity<AutoRentalListDTO> queryAutoRental(AutoRentalDTO probe, Integer pageNumber,
            Integer pageSize) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_QUERY_PATH);
        if (null!=pageNumber && null!=pageSize) {
             uriBuilder = uriBuilder.queryParam("pageNumber", pageNumber);
             uriBuilder = uriBuilder.queryParam("pageSize", pageSize);
            //uriBuilder = uriBuilder.queryParam("offset", pageNumber);
            //uriBuilder = uriBuilder.queryParam("limit", pageSize);
        }
        URI url = uriBuilder.build().toUri();

        RequestEntity<AutoRentalDTO> request = RequestEntity.post(url).accept(mediaType).body(probe);
        ResponseEntity<AutoRentalListDTO> response = restTemplate.exchange(request, AutoRentalListDTO.class);

        return response;
    }

    @Override
    public ResponseEntity<AutoRentalListDTO> searchAutoRental(RentalSearchParams searchParams) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTALS_PATH);
        if (searchParams.getAutoId()!=null) {
            uriBuilder = uriBuilder.queryParam("autoId", searchParams.getAutoId());
        }
        if (searchParams.getRenterId()!=null) {
            uriBuilder = uriBuilder.queryParam("renterId", searchParams.getRenterId());
        }
        if (searchParams.getStartDate()!=null) {
            uriBuilder = uriBuilder.queryParam("startDate", searchParams.getStartDate());
        }
        if (searchParams.getEndDate()!=null) {
            uriBuilder = uriBuilder.queryParam("endDate", searchParams.getEndDate());
        }

        if (null!=searchParams.getPageNumber() && null!=searchParams.getPageSize()) {
            uriBuilder = uriBuilder.queryParam("pageNumber", searchParams.getPageNumber());
            uriBuilder = uriBuilder.queryParam("pageSize", searchParams.getPageSize());
            //uriBuilder = uriBuilder.queryParam("offset", searchParams.getPageNumber());
            //uriBuilder = uriBuilder.queryParam("limit", searchParams.getPageSize());
        }
        URI url = uriBuilder.build().toUri();

        RequestEntity<Void> request = RequestEntity.get(url)
                                        .accept(mediaType)
                                        .build();
        ResponseEntity<AutoRentalListDTO> response = restTemplate.exchange(request, AutoRentalListDTO.class);

        return response;
    }

    @Override
    public ResponseEntity<AutoRentalDTO> getAutoRental(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.get(url)
                .accept(mediaType)
                .build();
        ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(request, AutoRentalDTO.class);

        return response;
    }

    @Override
    public ResponseEntity<Void> hasAutoRental(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.head(url).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<AutoRentalDTO> updateAutoRental(String id, AutoRentalDTO autoRental) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        RequestEntity<AutoRentalDTO> request = RequestEntity.put(url)
                                                .accept(mediaType)
                                                .contentType(mediaType)
                                                .body(autoRental);
        ResponseEntity<AutoRentalDTO> response = restTemplate.exchange(request, AutoRentalDTO.class);
        
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAutoRental(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        
        RequestEntity<Void> request = RequestEntity.delete(url).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAllAutoRental() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTALS_PATH).build().toUri();

        RequestEntity<Void> request = RequestEntity.delete(url).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }
    
}
