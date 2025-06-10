package info.ejava.assignments.api.autorenters.client.autos;


import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
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
public class AutosAPIRestTemplate implements AutosAPI {
    protected URI baseUrl;
    protected RestTemplate restTemplate;
    protected MediaType mediaType;

    public AutosAPIRestTemplate(RestTemplate restTemplate, ServerConfig serverConfig, MediaType mediaType) {
        this.baseUrl = serverConfig.getBaseUrl();
        this.restTemplate = restTemplate;
        this.mediaType = mediaType;
    }
    
    public AutosAPIRestTemplate withRestTemplate(RestTemplate restTemplate) {
        ServerConfig serverConfig = new ServerConfig().withBaseUrl(baseUrl).build();
        return new AutosAPIRestTemplate(restTemplate, serverConfig, mediaType);
    }

    @Override
    public ResponseEntity<AutoDTO> createAuto(AutoDTO auto) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTOS_PATH).build().toUri();

        RequestEntity<AutoDTO> request = RequestEntity.post(url)
                .accept(mediaType)
                .contentType(mediaType)
                .body(auto);
        ResponseEntity<AutoDTO> response = restTemplate.exchange(request, AutoDTO.class);
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
    public ResponseEntity<AutoListDTO> queryAutos(AutoDTO probe, Integer pageNumber, Integer pageSize) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AUTOS_QUERY_PATH);
        if (null!=pageNumber && null!=pageSize) {
            uriBuilder = uriBuilder.queryParam("pageNumber", pageNumber);
            uriBuilder = uriBuilder.queryParam("pageSize", pageSize);
        }
        URI url = uriBuilder.build().toUri();

        RequestEntity<AutoDTO> request = RequestEntity.post(url)
                .accept(mediaType)
                .body(probe);
        ResponseEntity<AutoListDTO> response = restTemplate.exchange(request, AutoListDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<AutoListDTO> searchAutos(AutoSearchParams searchParams) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUrl).path(AUTOS_PATH);
        if (searchParams.getMinPassengersInclusive()!=null) {
            uriBuilder = uriBuilder.queryParam("minPassengers", searchParams.getMinPassengersInclusive());
        }
        if (searchParams.getMaxPassengersInclusive()!=null) {
            uriBuilder = uriBuilder.queryParam("maxPassengers", searchParams.getMaxPassengersInclusive());
        }
        if (searchParams.getMinDailyRateInclusive()!=null) {
            uriBuilder = uriBuilder.queryParam("minDailyRate", searchParams.getMinDailyRateInclusive());
        }
        if (searchParams.getMaxDailyRateExclusive()!=null) {
            uriBuilder = uriBuilder.queryParam("maxDailyRate", searchParams.getMaxDailyRateExclusive());
        }

        if (null!=searchParams.getPageNumber() && null!=searchParams.getPageSize()) {
            uriBuilder = uriBuilder.queryParam("pageNumber", searchParams.getPageNumber());
            uriBuilder = uriBuilder.queryParam("pageSize", searchParams.getPageSize());
        }
        URI url = uriBuilder.build().toUri();

        RequestEntity<Void> request = RequestEntity.get(url)
                .accept(mediaType)
                .build();
        ResponseEntity<AutoListDTO> response = restTemplate.exchange(request, AutoListDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<AutoDTO> getAuto(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.get(url)
                .accept(mediaType)
                .build();
        ResponseEntity<AutoDTO> response = restTemplate.exchange(request, AutoDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> hasAuto(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.head(url).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<AutoDTO> updateAuto(String id, AutoDTO auto) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        RequestEntity<AutoDTO> request = RequestEntity.put(url)
                .accept(mediaType)
                .contentType(mediaType)
                .body(auto);
        ResponseEntity<AutoDTO> response = restTemplate.exchange(request, AutoDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAuto(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.delete(url).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAllAutos() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTOS_PATH).build().toUri();

        RequestEntity<Void> request = RequestEntity.delete(url).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }
}
