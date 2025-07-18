package info.ejava.assignments.api.autorenters.client.autorentals;


import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalListDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.examples.common.web.ServerConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AutoRentalsAPIRestClient implements AutoRentalsAPI {
    protected URI baseUrl;
    protected RestClient restClient;
    protected MediaType mediaType;

    public AutoRentalsAPIRestClient(RestClient restClient, ServerConfig serverConfig, MediaType mediaType) {
        this.baseUrl = serverConfig.getBaseUrl();
        this.restClient = restClient;
        this.mediaType = mediaType;
    }
    
    public AutoRentalsAPIRestClient withRestClient(RestClient restClient) {
        ServerConfig serverConfig = new ServerConfig().withBaseUrl(baseUrl).build();
        return new AutoRentalsAPIRestClient(restClient, serverConfig, mediaType);
    }

    @Override
    public ResponseEntity<AutoRentalDTO> createAutoRental(AutoRentalDTO autoRental) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTALS_PATH).build().toUri();
        
        ResponseEntity<AutoRentalDTO> response = restClient.post().uri(url).accept(mediaType)
                                            .contentType(mediaType).body(autoRental)
                                            .retrieve().toEntity(AutoRentalDTO.class);
        return response;
    }

    
    /**
     * This query mechanism passes an example "probe" that is used to find matches
     * that exactly matches non-null fields
     * @param probe
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @Override
    public ResponseEntity<AutoRentalListDTO> queryAutoRental(AutoRentalDTO probe, Integer pageNumber, Integer pageSize) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_QUERY_PATH);
        if (null!=pageNumber && null!=pageSize) {
             uriBuilder = uriBuilder.queryParam("pageNumber", pageNumber);
             uriBuilder = uriBuilder.queryParam("pageSize", pageSize);
            //uriBuilder = uriBuilder.queryParam("offset", pageNumber);
            //uriBuilder = uriBuilder.queryParam("limit", pageSize);
        }
        URI url = uriBuilder.build().toUri();

        ResponseEntity<AutoRentalListDTO> response = restClient.post().uri(url).accept(mediaType)
                                                .body(probe).retrieve().toEntity(AutoRentalListDTO.class);
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

        ResponseEntity<AutoRentalListDTO> response = restClient.get().uri(url).accept(mediaType).retrieve().toEntity(AutoRentalListDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<AutoRentalDTO> getAutoRental(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.get(url)
                .accept(mediaType)
                .build();
        ResponseEntity<AutoRentalDTO> response = restClient.get().uri(url)
                                            .accept(mediaType).retrieve().toEntity(AutoRentalDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> hasAutoRental(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        ResponseEntity<Void> response = restClient.head().uri(url).retrieve().toEntity(Void.class);
        return response;
    }

    @Override
    public ResponseEntity<AutoRentalDTO> updateAutoRental(String id, AutoRentalDTO autoRental) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        ResponseEntity<AutoRentalDTO> response = restClient.put().uri(url).accept(mediaType)
                                            .contentType(mediaType).body(autoRental).retrieve().toEntity(AutoRentalDTO.class);
        
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAutoRental(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTAL_PATH).build(id);

        ResponseEntity<Void> response = restClient.delete().uri(url).retrieve().toEntity(Void.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAllAutoRental() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_RENTALS_PATH).build().toUri();

        ResponseEntity<Void> response = restClient.delete().uri(url).retrieve().toEntity(Void.class);
        return response;
    }

}
