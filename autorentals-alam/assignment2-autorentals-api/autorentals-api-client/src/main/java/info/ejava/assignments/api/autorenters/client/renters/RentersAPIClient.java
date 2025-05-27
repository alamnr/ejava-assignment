package info.ejava.assignments.api.autorenters.client.renters;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.web.ServerConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.URI;

/**
 * This class has been modified to leverage Spring HTTP Interface API, while retaining
 * backward-compatibility with legacy test infrastructure. The original intent of this
 * class was to encapsulate the details of making each HTTP call. It still does that,
 * but with the use of the annotated interface and dynamic proxy.
 * If something breaks or something custom must take place, we retain the ability to
 * put that in place here. This is not an all-or-nothing choice.
 */
@Setter
@Getter
public class RentersAPIClient implements RentersAPI {
    private final URI baseUrl;
    private final RentersHttpIface rentersHttpAPI;
    @Setter(AccessLevel.NONE)
    private final RestClient restClient;

    public RentersAPIClient(RestTemplate restTemplate, ServerConfig serverConfig, MediaType alwaysJson) {
        this(restTemplate, serverConfig.getBaseUrl());
    }

    public RentersAPIClient(RestTemplate restTemplate, URI baseUrl) {
        this.baseUrl = baseUrl;

        this.restClient = RestClient.builder(restTemplate)
                .baseUrl(baseUrl.toString())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        this.rentersHttpAPI = factory.createClient(RentersJSONIfaceMapping.class);
    }

    @Override
    public RentersAPIClient withRestTemplate(RestTemplate restTemplate) {
        ServerConfig serverConfig = new ServerConfig().withBaseUrl(baseUrl).build();
        return new RentersAPIClient(restTemplate, baseUrl);
    }

    @Override
    public ResponseEntity<RenterDTO> createRenter(RenterDTO renter) {
        return rentersHttpAPI.createRenter(renter);
//        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RENTERS_PATH).build().toUri();
//
//        RequestEntity<RenterDTO> request = RequestEntity.post(url)
//                .accept(mediaType)
//                .contentType(mediaType)
//                .body(renter);
//        ResponseEntity<RenterDTO> response = restTemplate.exchange(request, RenterDTO.class);
//        return response;
    }

    @Override
    public ResponseEntity<RenterListDTO> getRenters(Integer pageNumber, Integer pageSize) {
        return rentersHttpAPI.getRenters(pageNumber, pageSize);
//        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUrl).path(RENTERS_PATH);
//        if (null!=pageNumber && null!=pageSize) {
//            uriBuilder = uriBuilder.queryParam("pageNumber", pageNumber);
//            uriBuilder = uriBuilder.queryParam("pageSize", pageSize);
//        }
//        URI url = uriBuilder.build().toUri();
//
//        RequestEntity request = RequestEntity.get(url)
//                .accept(mediaType)
//                .build();
//        ResponseEntity<RenterListDTO> response = restTemplate.exchange(request, RenterListDTO.class);
//        return response;
    }

    @Override
    public ResponseEntity<RenterDTO> getRenter(String id) {
        return rentersHttpAPI.getRenter(id);
//        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RENTER_PATH).build(id);
//
//        RequestEntity request = RequestEntity.get(url)
//                .accept(mediaType)
//                .build();
//        ResponseEntity<RenterDTO> response = restTemplate.exchange(request, RenterDTO.class);
//        return response;
    }

    @Override
    public ResponseEntity<Void> hasRenter(String id) {
        return rentersHttpAPI.hasRenter(id);
//        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RENTER_PATH).build(id);
//
//        RequestEntity request = RequestEntity.head(url).build();
//        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
//        return response;
    }

    @Override
    public ResponseEntity<RenterDTO> updateRenter(String id, RenterDTO renter) {
        return rentersHttpAPI.updateRenter(id, renter);
//        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RENTER_PATH).build(id);
//
//        RequestEntity request = RequestEntity.put(url)
//                .contentType(mediaType)
//                .accept(mediaType)
//                .body(renter);
//        ResponseEntity<RenterDTO> response = restTemplate.exchange(request, RenterDTO.class);
//        return response;
    }

    @Override
    public ResponseEntity<Void> removeRenter(String id) {
        return rentersHttpAPI.removeRenter(id);
//        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RENTER_PATH).build(id);
//
//        RequestEntity request = RequestEntity.delete(url)
//                .accept(mediaType)
//                .build();
//        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
//        return response;
    }

    @Override
    public ResponseEntity<Void> removeAllRenters() {
        return rentersHttpAPI.removeAllRenters();
//        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RENTERS_PATH).build().toUri();
//
//        RequestEntity request = RequestEntity.delete(url)
//                .accept(mediaType)
//                .build();
//        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
//        return response;
    }
}
