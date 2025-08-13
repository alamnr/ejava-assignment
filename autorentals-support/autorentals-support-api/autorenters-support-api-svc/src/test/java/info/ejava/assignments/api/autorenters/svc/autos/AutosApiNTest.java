package info.ejava.assignments.api.autorenters.svc.autos;

import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.svc.ProvidedApiAutoRenterTestConfiguration;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes= {ProvidedApiAutoRenterTestConfiguration.class},
        webEnvironment = RANDOM_PORT)
@SpringBootConfiguration
@EnableAutoConfiguration
@ActiveProfiles("test")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AutosApiNTest {
    @Autowired
    private AutosAPI autosAPIClient;
    @Autowired
    private AutoDTOFactory autoFactory;

    @Test
    void can_add_auto() {
        //given
        AutoDTO auto = autoFactory.make();
        //when
        ResponseEntity<AutoDTO> response = autosAPIClient.createAuto(auto);
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
        ResponseEntity<AutoDTO> response = autosAPIClient.getAuto(existingAuto.getId());
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
        RestClientResponseException ex = catchThrowableOfType(()-> autosAPIClient.getAuto(nonExistantId),
                RestClientResponseException.class);
        //then
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errorMsg = JsonUtil.instance().unmarshal(ex.getResponseBodyAsString(), MessageDTO.class);
        then(errorMsg.getTimestamp()).isNotNull();
        then(errorMsg.getDescription()).isEqualTo(String.format("Auto[%s] not found", nonExistantId));
    }

    @Test
    void existing_auto_returns_ok() {
        //given
        AutoDTO existingAuto = populate(1).get(0);
        //when
        ResponseEntity<Void> response = autosAPIClient.hasAuto(existingAuto.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isNull();
    }

    @Test
    void non_existing_auto_returns_notfound() {
        //given
        String nonExistingId = "nonExistingId";
        //when
        RestClientResponseException ex = catchThrowableOfType(() -> autosAPIClient.hasAuto(nonExistingId),
                RestClientResponseException.class);
        //then
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void can_update_auto() {
        //given
        AutoDTO existingAuto = populate(1).get(0);
        BigDecimal updatedRate = existingAuto.getDailyRate().add(new BigDecimal(10));
        AutoDTO updatedAuto = existingAuto.withDailyRate(updatedRate);
        //when
        ResponseEntity<AutoDTO> response = autosAPIClient.updateAuto(existingAuto.getId(), updatedAuto);
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AutoDTO responseAuto = response.getBody();
        then(responseAuto.getDailyRate()).isEqualTo(updatedRate);
        then(responseAuto).isEqualTo(updatedAuto);
    }

    @Test
    void can_delete_a_auto() {
        //given
        List<AutoDTO> autos = new ArrayList<>(populate(3));
        AutoDTO existingAuto = autos.remove(0);
        //when
        ResponseEntity<Void> response = autosAPIClient.removeAuto(existingAuto.getId());
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        autos.stream().forEach(h->{
            then(autosAPIClient.hasAuto(h.getId()).getStatusCode()).isEqualTo(HttpStatus.OK);
        });
    }

    @Test
    void can_delete_all_autos() {
        //given
        List<AutoDTO> autos = new ArrayList<>(populate(3));
        //when
        ResponseEntity<Void> response = autosAPIClient.removeAllAutos();
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        autos.stream().forEach(h->{
            then(autoExists(h.getId())).isFalse();
        });
    }

    private boolean autoExists(String id) {
        try {
            return autosAPIClient.hasAuto(id).getStatusCode().equals(HttpStatus.OK);
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    private List<AutoDTO> populate(int count) {
        return IntStream.range(0, count)
                .mapToObj(i-> autoFactory.make())
                .map(h -> autosAPIClient.createAuto(h).getBody())
                .toList();
    }

    List<AutoDTO> queryAutos = null;
    @Nested
    class query_paging {
        @BeforeEach
        void cleanup() {
            if (null==queryAutos) {
                autosAPIClient.removeAllAutos();
                queryAutos=populate(20);
            }
        }

        @Test
        void can_get_autos() {
            //given
            List<AutoDTO> expectedAutos = queryAutos;
            //when
            ResponseEntity<AutoListDTO> response = autosAPIClient.searchAutos(AutoSearchParams.builder().build());
            //then
            then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<AutoDTO> foundAutos = response.getBody().getContents();
            then(foundAutos).isNotNull();
            then(new HashSet<>(foundAutos)).isEqualTo(new HashSet<>(expectedAutos));
        }

        @Test
        void can_get_autos_paged() {
            //given
            List<AutoDTO> expectedAutos = queryAutos;
            int pageSize=3;
            int pageNum=0;
            List<AutoDTO> returnedAutos = new ArrayList<>();
            do {
                AutoSearchParams searchParams = AutoSearchParams.builder().build().page(pageNum, pageSize);
                //when
                ResponseEntity<AutoListDTO> response = autosAPIClient.searchAutos(searchParams);
                //then
                AutoListDTO autos = response.getBody();
                then(response.getStatusCode().is2xxSuccessful()).isTrue();
                then(autos.getContents()).isNotNull();
                then(autos.getContents().size()).isLessThanOrEqualTo(pageSize);
                returnedAutos.addAll(autos.getContents());
                if (autos.getContents().isEmpty()) {
                    break;
                }
                pageNum += 1;
            } while (true);

            then(new HashSet<>(returnedAutos)).isEqualTo(new HashSet<>(expectedAutos));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class search {
        @BeforeAll
        void cleanup() {
            autosAPIClient.removeAllAutos();
            queryAutos=populate(20);
        }

        @ParameterizedTest
        @MethodSource("autos_by_passengers_args")
        void by_passenger_count(int minCount, int maxCount, List<AutoDTO> autos) {
            //given
            AutoSearchParams searchParams = AutoSearchParams.builder()
                    .build()
                    .passengersWithin(minCount, maxCount);
            //when
            AutoListDTO matches = autosAPIClient.searchAutos(searchParams).getBody();
            //then
            then(matches.getContents()).containsExactlyInAnyOrder(autos.toArray(new AutoDTO[]{}));
        }

        Stream<Arguments> autos_by_passengers_args() {
            return autos_by_passengers().entrySet().stream()
                    .map(entry->Arguments.of(entry.getKey(), entry.getKey(), entry.getValue()));
        }

        @ParameterizedTest
        @MethodSource("autos_by_daily_rate_args")
        void by_daily_rate(int minRate, int maxRate, List<AutoDTO> autos) {
            //given
            AutoSearchParams searchParams = AutoSearchParams.builder()
                    .build()
                    .dailyRateWithin(minRate, maxRate);
            //when
            AutoListDTO matches = autosAPIClient.searchAutos(searchParams).getBody();
            //then
            then(matches.getContents()).containsExactlyInAnyOrder(autos.toArray(new AutoDTO[]{}));
        }

        Stream<Arguments> autos_by_daily_rate_args() {
            return autos_by_daily_rate().entrySet().stream()
                    .map(entry->Arguments.of(entry.getKey(), entry.getKey()+10, entry.getValue()));
        }
    }

    /**
     * Returns a map of AutoDTOs keyed by daily rate truncated to lower dollar.
     * @return map of autos by passengers
     */
    Map<Integer, List<AutoDTO>> autos_by_daily_rate() {
        return queryAutos.stream().collect(
                LinkedMultiValueMap::new,
                (map, element)->map.add(roundDown(element.getDailyRate()), element),
                (map1, map2)->{}
        );
    }

    int roundDown(BigDecimal amount) {
        return (amount.intValue()/10) * 10;
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class query_by {
        @BeforeAll
        void cleanup() {
            autosAPIClient.removeAllAutos();
            queryAutos=populate(20);
        }

        //query by unique ID
        @Test
        void equals_id() {
            //given
            AutoDTO auto = queryAutos.get(0);
            AutoDTO probe = AutoDTO.builder()
                    .id(auto.getId())
                    .build();
            //when
            AutoListDTO autos = autosAPIClient.queryAutos(probe, null, null).getBody();
            //then
            then(autos.getContents()).hasSize(1);
            Optional<AutoDTO> match = autos.getContents().stream().findFirst();
            then(match).hasValue(auto);
        }

        //search by all values; making 1 property invalid each time after first attempt
        @ParameterizedTest
        @ValueSource(ints = {0,1,2,3,4,5,6})
        void by_property(int index) {
            //given
            AutoDTO auto = queryAutos.get(0);
            AutoDTO probe = auto.toBuilder().build();
            switch (index) {
                case 1:
                    probe.setId("NOT_FOUND");
                    break;
                case 2:
                    probe.setPassengers(1_000_000);
                    break;
                case 3:
                    probe.setFuelType("NOT_FOUND");
                    break;
                case 4:
                    probe.setMake("NOT_FOUND");
                    break;
                case 5:
                    probe.setModel("NOT_FOUND");
                    break;
                case 6:
                    probe.setDailyRate(new BigDecimal(1_000_000));
                    break;
                default:
            }
            //when
            AutoListDTO autos = autosAPIClient.queryAutos(probe, null, null).getBody();
            //then
            Optional<AutoDTO> match = autos.getContents().stream().findFirst();
            if (0 == index) { //all values valid, auto found
                then(autos.getContents()).hasSize(1);
                then(match).as("by matching properties").hasValue(auto);
            } else { //invalid value supplued, nothing found
                then(autos.getContents()).hasSize(0);
            }
        }

        @ParameterizedTest
        @MethodSource("autos_by_passengers_args")
        void by_passengers(Integer passengerCount, List<AutoDTO> expectedMatches) {
            //given
            AutoDTO probe = AutoDTO.builder().passengers(passengerCount).build();
            //when
            AutoListDTO matches = autosAPIClient.queryAutos(probe, null, null).getBody();
            //then
            then(matches.getContents()).containsExactlyInAnyOrder(expectedMatches.toArray(new AutoDTO[0]));
        }

        Stream<Arguments> autos_by_passengers_args() {
            return autos_by_passengers().entrySet().stream()
                    .map(entry->Arguments.of(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Returns a map of AutoDTOs keyed by passenger count.
     * @return map of autos by passengers
     */
    Map<Integer, List<AutoDTO>> autos_by_passengers() {
        return queryAutos.stream().collect(
                LinkedMultiValueMap::new,
                (map, element)->map.add(element.getPassengers(), element),
                (map1, map2)->{}
        );
    }
}
