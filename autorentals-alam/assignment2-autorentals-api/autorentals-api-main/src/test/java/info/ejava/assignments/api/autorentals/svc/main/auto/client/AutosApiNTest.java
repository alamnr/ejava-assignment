package info.ejava.assignments.api.autorentals.svc.main.auto.client;


import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.ProvidedApiAutoRenterTestConfiguration;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.autos.AutosJSONHttpIfaceMapping;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes={ProvidedApiAutoRenterTestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@SpringBootConfiguration
//@EnableAutoConfiguration
@ActiveProfiles("test")
@Slf4j
public class AutosApiNTest {
    @Autowired
    private AutoDTOFactory autoDtoFactory ;
    @Autowired @Qualifier("autosHttpIfaceJson")
    private AutosJSONHttpIfaceMapping autosHttpIfaceJson;

    @Test
    void can_add_auto() {
        //given
        AutoDTO auto = autoDtoFactory.make();
        //when
        ResponseEntity<AutoDTO> response = autosHttpIfaceJson.createAuto(auto);
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        AutoDTO addedAuto = response.getBody();
        then(addedAuto.getId()).isNotNull();
        then(addedAuto).isEqualTo(auto.withId(addedAuto.getId()));

        URI location = response.getHeaders().getLocation();
        URI expectedLocation = UriComponentsBuilder.fromUriString(AutosAPI.AUTO_PATH).build(addedAuto.getId());
        then(location.toString()).endsWith(expectedLocation.toString());
    }

    @Test
    void can_get_existing_auto() {
        //given
        AutoDTO existingAuto = populate(1).get(0);
        //when
        ResponseEntity<AutoDTO> response = autosHttpIfaceJson.getAuto(existingAuto.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoDTO responseAuto = response.getBody();
        then(responseAuto).isEqualTo(existingAuto);
    }

    @Test
    void get_non_existant_auto_returns_notfound() {
        //given
        String nonExistantId = "nonExistantId";
        //when
        RestClientResponseException ex = catchThrowableOfType(()-> autosHttpIfaceJson.getAuto(nonExistantId),
                RestClientResponseException.class);
        //then
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errorMsg = JsonUtil.instance().unmarshal(ex.getResponseBodyAsString(), MessageDTO.class);
        then(errorMsg.getTimestamp()).isNotNull();
        then(errorMsg.getDescription()).isEqualTo(String.format("auto[%s] not found", nonExistantId));
    }

    @Test
    void existing_auto_returns_ok() {
        //given
        AutoDTO existingAuto = populate(1).get(0);
        //when
        ResponseEntity<Void> response = autosHttpIfaceJson.hasAuto(existingAuto.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isNull();
    }

    @Test
    void non_existing_auto_returns_notfound() {
        //given
        String nonExistingId = "nonExistingId";
        //when
        RestClientResponseException ex = catchThrowableOfType(
                () -> autosHttpIfaceJson.hasAuto(nonExistingId),
                RestClientResponseException.class);
        //then
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void can_update_auto() {
        //given
        AutoDTO existingAuto = populate(1).get(0);
        String updatedModel = existingAuto.getModel()+" updated";
        AutoDTO updatedAuto = existingAuto.withModel(updatedModel);
        //when
        ResponseEntity<AutoDTO> response = autosHttpIfaceJson.updateAuto(existingAuto.getId(), updatedAuto);
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoDTO responseAuto = response.getBody();
        then(responseAuto.getModel()).isEqualTo(updatedModel);
        then(responseAuto).isEqualTo(updatedAuto);
    }

    @Test
    void can_delete_an_auto() {
        //given
        List<AutoDTO> autos = new ArrayList<>(populate(3));
        AutoDTO existingAuto = autos.remove(0);
        //when
        ResponseEntity<Void> response = autosHttpIfaceJson.removeAuto(existingAuto.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        autos.stream().forEach(h->{
            then(autosHttpIfaceJson.hasAuto(h.getId()).getStatusCode()).isEqualTo(HttpStatus.OK);
        });
    }

    @Test
    void can_delete_all_autos() {
        //given
        List<AutoDTO> autos = new ArrayList<>(populate(3));
        //when
        ResponseEntity<Void> response = autosHttpIfaceJson.removeAllAutos();
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        autos.stream().forEach(h->{
            then(autoExists(h.getId())).isFalse();
        });
    }

    private boolean autoExists(String id) {
        try {
            return autosHttpIfaceJson.hasAuto(id).getStatusCode().equals(HttpStatus.OK);
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    private List<AutoDTO> populate(int count) {
        return IntStream.range(0, count)
                .mapToObj(i-> autoDtoFactory.make())
                .map(h -> autosHttpIfaceJson.createAuto(h).getBody())
                .toList();
    }

    List<AutoDTO> queryAutos = null;
    @Nested
    class query {
        @BeforeEach
        void cleanup() {
            if (null==queryAutos) {
                autosHttpIfaceJson.removeAllAutos();
                queryAutos=populate(20);
            }
        }

        @Test
        void can_get_autos() {
            //given
            List<AutoDTO> expectedAutos = queryAutos;
            //when
            //AutoSearchParams searchParams = AutoSearchParams.builder().pageNumber(0).pageSize(0).build();
            //ResponseEntity<AutoListDTO> response = autosHttpIfaceJson.searchAutos(searchParams);
            ResponseEntity<AutoListDTO> response = autosHttpIfaceJson.searchAutosList(null, null, null,
                                         null,  0 , 0);
            
            //then
            then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<AutoDTO> foundAutos = response.getBody().getAutos();
            then(foundAutos).isNotNull();
            then(new HashSet<>(foundAutos)).isEqualTo(new HashSet<>(expectedAutos));
            
            ResponseEntity<AutoListDTO> response_2 = autosHttpIfaceJson.queryAutos(queryAutos.get(0), 0, 0);
            then(response_2.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            log.info("response-2 - {}", response_2.getBody());
            
        }

        @Test
        void can_get_autos_paged_list() {
            //given
            List<AutoDTO> expectedAutos = queryAutos;
            int pageSize=3;
            int pageNum=0;
            List<AutoDTO> returnedAutos = new ArrayList<>();
            do {
                //when
                //AutoSearchParams searchParams = AutoSearchParams.builder().pageNumber(0).pageSize(0).build();

                //ResponseEntity<AutoListDTO> response = autosHttpIfaceJson.searchAutos(searchParams);
                ResponseEntity<AutoListDTO> response = autosHttpIfaceJson.searchAutosList(null, null, null,
                                         null, pageNum , pageSize);
                
                
            
                AutoListDTO autos = response.getBody();
                //then
                then(response.getStatusCode().is2xxSuccessful()).isTrue();
                then(autos.getAutos()).isNotNull();
                then(autos.getAutos().size()).isLessThanOrEqualTo(pageSize);
                returnedAutos.addAll(autos.getAutos());
                if (autos.getAutos().isEmpty()) {
                    break;
                }
                pageNum += 1;
            } while (true);

            then(new HashSet<>(returnedAutos)).isEqualTo(new HashSet<>(expectedAutos));

            ResponseEntity<AutoListDTO> response_2 = autosHttpIfaceJson.queryAutos(queryAutos.get(0), 0, 0);
            then(response_2.getStatusCode().is2xxSuccessful()).isTrue();
            log.info("response-2 - {}", response_2.getBody());
            ResponseEntity<AutoListDTO> response_3 = autosHttpIfaceJson.searchAutosList(0, 0, 1,
                                         5, 0 , 0);
            then(response_3.getStatusCode().is2xxSuccessful()).isTrue();
            log.info("response-3 - {}", response_3.getBody());
        }
    }
}

