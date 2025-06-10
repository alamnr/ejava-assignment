package info.ejava.assignments.api.autorenters.client.autos;


import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.examples.common.web.ServerConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AutosAPIWebClient implements AutosAPI {
    protected URI baseUrl;
    protected WebClient webClient;
    protected MediaType mediaType;

    public AutosAPIWebClient(WebClient webClient, ServerConfig serverConfig, MediaType mediaType) {
        this.baseUrl = serverConfig.getBaseUrl();
        this.webClient = webClient;
        this.mediaType = mediaType;
    }
    
    public AutosAPIWebClient withWebClient(WebClient webClient) {
        ServerConfig serverConfig = new ServerConfig().withBaseUrl(baseUrl).build();
        return new AutosAPIWebClient(webClient, serverConfig, mediaType);
    }

    @Override
    public ResponseEntity<AutoDTO> createAuto(AutoDTO auto) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTOS_PATH).build().toUri();
        
        ResponseEntity<AutoDTO> response = webClient.post().uri(url).accept(mediaType)
                                            .contentType(mediaType).bodyValue(auto)
                                            .retrieve().toEntity(AutoDTO.class).block();
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
            // uriBuilder = uriBuilder.queryParam("pageNumber", pageNumber);
            // uriBuilder = uriBuilder.queryParam("pageSize", pageSize);
            uriBuilder = uriBuilder.queryParam("offset", pageNumber);
            uriBuilder = uriBuilder.queryParam("limit", pageSize);
        }
        URI url = uriBuilder.build().toUri();

        ResponseEntity<AutoListDTO> response = webClient.post().uri(url).accept(mediaType)
                                                .bodyValue(probe).retrieve().toEntity(AutoListDTO.class).block();
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
            // uriBuilder = uriBuilder.queryParam("pageNumber", searchParams.getPageNumber());
            // uriBuilder = uriBuilder.queryParam("pageSize", searchParams.getPageSize());
             uriBuilder = uriBuilder.queryParam("offset", searchParams.getPageNumber());
            uriBuilder = uriBuilder.queryParam("limit", searchParams.getPageSize());
        }
        URI url = uriBuilder.build().toUri();

        ResponseEntity<AutoListDTO> response = webClient.get().uri(url).accept(mediaType)
                                                .retrieve().toEntity(AutoListDTO.class).block();
        return response;
    }

    @Override
    public ResponseEntity<AutoDTO> getAuto(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.get(url)
                .accept(mediaType)
                .build();
        ResponseEntity<AutoDTO> response = webClient.get().uri(url)
                                            .accept(mediaType).retrieve().toEntity(AutoDTO.class).block();
        return response;
    }

    @Override
    public ResponseEntity<Void> hasAuto(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        ResponseEntity<Void> response = webClient.head().uri(url).retrieve().toEntity(Void.class).block();
        return response;
    }

    @Override
    public ResponseEntity<AutoDTO> updateAuto(String id, AutoDTO auto) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        ResponseEntity<AutoDTO> response = webClient.put().uri(url).accept(mediaType)
                                            .contentType(mediaType).bodyValue(auto).retrieve().toEntity(AutoDTO.class).block();
        
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAuto(String id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTO_PATH).build(id);

        ResponseEntity<Void> response = webClient.delete().uri(url).retrieve().toEntity(Void.class).block();
        return response;
    }

    @Override
    public ResponseEntity<Void> removeAllAutos() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(AUTOS_PATH).build().toUri();

        ResponseEntity<Void> response = webClient.delete().uri(url).retrieve().toEntity(Void.class).block();
        return response;
    }
}
