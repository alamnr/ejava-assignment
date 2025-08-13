package info.ejava.assignments.api.autorenters.svc.renters;

import info.ejava.assignments.api.autorenters.svc.ProvidedApiAutoRenterTestConfiguration;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(classes={ProvidedApiAutoRenterTestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootConfiguration
@EnableAutoConfiguration
@ActiveProfiles("test")
public class RentersApiNTest {
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired
    private RentersAPI rentersAPIClient;

    @Test
    void can_add_renter() {
        //given
        RenterDTO renter = renterFactory.make();
        //when
        ResponseEntity<RenterDTO> response = rentersAPIClient.createRenter(renter);
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        RenterDTO addedRenter = response.getBody();
        then(addedRenter.getId()).isNotNull();
        then(addedRenter).isEqualTo(renter.withId(addedRenter.getId()));

        URI location = response.getHeaders().getLocation();
        URI expectedLocation = UriComponentsBuilder.fromUriString(RentersAPI.RENTER_PATH).build(addedRenter.getId());
        then(location.toString()).endsWith(expectedLocation.toString());
    }

    @Test
    void can_get_existing_renter() {
        //given
        RenterDTO existingRenter = populate(1).get(0);
        //when
        ResponseEntity<RenterDTO> response = rentersAPIClient.getRenter(existingRenter.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        RenterDTO responseRenter = response.getBody();
        then(responseRenter).isEqualTo(existingRenter);
    }

    @Test
    void get_non_existant_renter_returns_notfound() {
        //given
        String nonExistantId = "nonExistantId";
        //when
        RestClientResponseException ex = catchThrowableOfType(()-> rentersAPIClient.getRenter(nonExistantId),
                RestClientResponseException.class);
        //then
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errorMsg = JsonUtil.instance().unmarshal(ex.getResponseBodyAsString(), MessageDTO.class);
        then(errorMsg.getTimestamp()).isNotNull();
        then(errorMsg.getDescription()).isEqualTo(String.format("Renter[%s] not found", nonExistantId));
    }

    @Test
    void existing_renter_returns_ok() {
        //given
        RenterDTO existingRenter = populate(1).get(0);
        //when
        ResponseEntity<Void> response = rentersAPIClient.hasRenter(existingRenter.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isNull();
    }

    @Test
    void non_existing_renter_returns_notfound() {
        //given
        String nonExistingId = "nonExistingId";
        //when
        RestClientResponseException ex = catchThrowableOfType(
                () -> rentersAPIClient.hasRenter(nonExistingId),
                RestClientResponseException.class);
        //then
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void can_update_renter() {
        //given
        RenterDTO existingRenter = populate(1).get(0);
        LocalDate updatedDob = existingRenter.getDob().plus(10, ChronoUnit.YEARS);
        RenterDTO updatedRenter = existingRenter.withDob(updatedDob);
        //when
        ResponseEntity<RenterDTO> response = rentersAPIClient.updateRenter(existingRenter.getId(), updatedRenter);
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        RenterDTO responseRenter = response.getBody();
        then(responseRenter.getDob()).isEqualTo(updatedDob);
        then(responseRenter).isEqualTo(updatedRenter);
    }

    @Test
    void can_delete_a_renter() {
        //given
        List<RenterDTO> renters = new ArrayList<>(populate(3));
        RenterDTO existingRenter = renters.remove(0);
        //when
        ResponseEntity<Void> response = rentersAPIClient.removeRenter(existingRenter.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        renters.stream().forEach(h->{
            then(rentersAPIClient.hasRenter(h.getId()).getStatusCode()).isEqualTo(HttpStatus.OK);
        });
    }

    @Test
    void can_delete_all_renters() {
        //given
        List<RenterDTO> renters = new ArrayList<>(populate(3));
        //when
        ResponseEntity<Void> response = rentersAPIClient.removeAllRenters();
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        renters.stream().forEach(h->{
            then(renterExists(h.getId())).isFalse();
        });
    }

    private boolean renterExists(String id) {
        try {
            return rentersAPIClient.hasRenter(id).getStatusCode().equals(HttpStatus.OK);
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    private List<RenterDTO> populate(int count) {
        return IntStream.range(0, count)
                .mapToObj(i-> renterFactory.make())
                .map(h -> rentersAPIClient.createRenter(h).getBody())
                .toList();
    }

    List<RenterDTO> queryRenters = null;
    @Nested
    class query {
        @BeforeEach
        void cleanup() {
            if (null==queryRenters) {
                rentersAPIClient.removeAllRenters();
                queryRenters=populate(20);
            }
        }

        @Test
        void can_get_renters() {
            //given
            List<RenterDTO> expectedRenters = queryRenters;
            //when
            ResponseEntity<RenterListDTO> response = rentersAPIClient.getRenters(null, null);
            //then
            then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<RenterDTO> foundRenters = response.getBody().getContents();
            then(foundRenters).isNotNull();
            then(new HashSet<>(foundRenters)).isEqualTo(new HashSet<>(expectedRenters));
        }

        @Test
        void can_get_renters_paged_list() {
            //given
            List<RenterDTO> expectedRenters = queryRenters;
            int pageSize=3;
            int pageNum=0;
            List<RenterDTO> returnedRenters = new ArrayList<>();
            do {
                //when
                ResponseEntity<RenterListDTO> response = rentersAPIClient.getRenters(pageNum, pageSize);
                RenterListDTO renters = response.getBody();
                //then
                then(response.getStatusCode().is2xxSuccessful()).isTrue();
                then(renters.getContents()).isNotNull();
                then(renters.getContents().size()).isLessThanOrEqualTo(pageSize);
                returnedRenters.addAll(renters.getContents());
                if (renters.getContents().isEmpty()) {
                    break;
                }
                pageNum += 1;
            } while (true);

            then(new HashSet<>(returnedRenters)).isEqualTo(new HashSet<>(expectedRenters));
        }
    }
}
