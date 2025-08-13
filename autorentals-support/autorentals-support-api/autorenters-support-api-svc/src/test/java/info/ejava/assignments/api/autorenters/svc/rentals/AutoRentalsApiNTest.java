package info.ejava.assignments.api.autorenters.svc.rentals;

import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.SearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.BDDSoftAssertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@SpringBootTest(classes={
//        AutoRentersNTestConfiguration.class,
//        },
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
//)
//@ActiveProfiles({"test"})
@Slf4j
public class AutoRentalsApiNTest {
    @Autowired
    AutoDTOFactory autoFactory;
    @Autowired
    RenterDTOFactory renterFactory;
    @Autowired
    private AutosAPI autosAPI;
    @Autowired
    private RentersAPI rentersAPI;
    @Autowired
    private ApiTestHelper<RentalDTO> testHelper;
    @Value("${autorentals.renter.minage:21}")
    private int minAge;

    private List<AutoDTO> autos;
    private List<RenterDTO> renters;
    private TimePeriod nextTimePeriod = new TimePeriod(LocalDate.now(), 7);

    @BeforeEach
    void populate() {
        Assumptions.assumeFalse(getClass().equals(AutoRentalsApiNTest.class),"should only run for derived class");

        if (autos ==null) {
            autosAPI.removeAllAutos();
            autos = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                AutoDTO auto = autosAPI.createAuto(autoFactory.make()).getBody();
                autos.add(auto);
            }
        }
        if (renters ==null) {
            rentersAPI.removeAllRenters();
            renters = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                RenterDTO renter = rentersAPI.createRenter(renterFactory.make()).getBody();
                renters.add(renter);
            }
        }
    }

    protected AutoDTO given_an_auto() {
        return autos.get(0);
    }
    protected RenterDTO given_a_renter() {
        return renters.get(0);
    }
    protected RenterDTO given_an_underage_renter() {
        return given_an_underage_renter(Period.ofYears(3));
    }
    protected RenterDTO given_an_underage_renter(TemporalAmount amount) {
        LocalDate underAgeBirthDate = LocalDate.now().minusYears(minAge).plus(amount);
        RenterDTO underAgeRenter = renterFactory.make(r->r.setDob(underAgeBirthDate));
        return rentersAPI.createRenter(underAgeRenter).getBody();
    }

    protected RenterDTO given_a_different_renter(String renterId) {
        return renters.stream()
                .filter(r-> !StringUtils.equals(renterId, r.getId()))
                .findFirst().orElseThrow();
    }
    protected TimePeriod given_a_time_period() {
        TimePeriod current = nextTimePeriod;
        nextTimePeriod = nextTimePeriod.next();
        return current;
    }
    protected RentalDTO given_a_proposal(AutoDTO auto, RenterDTO renter, TimePeriod timePeriod) {
        auto = Objects.requireNonNullElse(auto, given_an_auto());
        renter = Objects.requireNonNullElse(renter, given_a_renter());
        timePeriod = Objects.requireNonNullElse(timePeriod, given_a_time_period());
        return testHelper.makeProposal(auto, renter, timePeriod);
    }
    protected RentalDTO given_a_proposal() {
        AutoDTO auto = given_an_auto();
        RenterDTO renter = given_a_renter();
        TimePeriod timePeriod = given_a_time_period();
        return testHelper.makeProposal(auto, renter, timePeriod);
    }
    protected RentalDTO given_a_rental_contract(AutoDTO auto, RenterDTO renter, TimePeriod timePeriod) {
        RentalDTO proposal = given_a_proposal(auto, renter, timePeriod);
        return testHelper.createContract(proposal).getBody();
    }
    protected RentalDTO given_a_rental_contract() {
        return given_a_rental_contract(null, null, null);
    }

    /**
     * This test verifies tries to identify issues with test setup and
     * testHelper methods before getting dep into the tests.
     */
    @Test
    public void test_is_ready() {
        //sample auto from the server-side
        AutoDTO auto = given_an_auto();
        then(auto).isNotNull();
        then(auto.getId()).isNotEmpty();
        then(auto.getDailyRate()).isNotNull();

        //sample renter from the server-side
        RenterDTO renter = given_a_renter();
        then(renter).isNotNull();
        then(renter.getId()).isNotEmpty();
        then(renter.getFirstName()).isNotEmpty();

        //sample time period
        TimePeriod timePeriod = given_a_time_period();

        //client-side proposal request
        RentalDTO proposal = testHelper.makeProposal(auto, renter, timePeriod);
        then(proposal).as("makeProposal").isNotNull();
        BDDSoftAssertions softly = new BDDSoftAssertions();
        softly.then(testHelper.getRentalId(proposal)).as("proposal getRentalId").isNull();
        softly.then(testHelper.getAutoId(proposal)).as("proposal getAutoId").isEqualTo(auto.getId());
        softly.then(testHelper.getRenterId(proposal)).as("proposal getRenterId").isEqualTo(renter.getId());
        softly.then(testHelper.getStartDate(proposal)).as("proposal getStartDate").isEqualTo(timePeriod.getStartDate());
        softly.then(testHelper.getEndDate(proposal)).as("proposal getEndDate").isEqualTo(timePeriod.getEndDate());
        //filled in by server-side
        softly.then(testHelper.getAutoMakeModel(proposal)).as("proposal getAutoMakeModel").isNull();
        softly.then(testHelper.getRenterName(proposal)).as("proposal getRenterName").isNull();
        softly.then(testHelper.getAmount(proposal)).as("proposal getAmount").isNull();
        softly.then(testHelper.getStreetAddress(proposal)).as("proposal getStreetAddress").isNull();

        //rentalId
        String rentalId = "rental-123";
        testHelper.setRentalId(proposal, rentalId);
        softly.then(testHelper.getRentalId(proposal)).as("rentalId").isEqualTo(rentalId);

        //updates
        TimePeriod newTimePeriod = given_a_time_period();
        testHelper.setStartDate(proposal, newTimePeriod.getStartDate());
        testHelper.setEndDateDate(proposal, newTimePeriod.getEndDate());
        softly.then(testHelper.getStartDate(proposal)).as("updated startDate").isEqualTo(newTimePeriod.getStartDate());
        softly.then(testHelper.getEndDate(proposal)).as("updated endDate").isEqualTo(newTimePeriod.getEndDate());

        //server-side fake filled
        RentalDTO fake = testHelper.makePopulatedFake();
        softly.then(testHelper.getAutoMakeModel(fake)).as("fake getAutoMakeModel").isNotEmpty();
        softly.then(testHelper.getRenterName(fake)).as("fake getRenterName").isNotEmpty();
        softly.then(testHelper.getAmount(fake)).as("fake getAmount").isNotNull();
        softly.then(testHelper.getStreetAddress(fake)).as("fake getStreetAddress").isNotNull();

        softly.assertAll();
    }

    @Nested
    class new_rental {
        @Test
        void can_create_rental() {
            //given
            RentalDTO proposal = given_a_proposal();
            //when
            ResponseEntity<RentalDTO> response = testHelper.createContract(proposal);
            RentalDTO rental = response.getBody();
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(response.getStatusCode()).as("statusCode").isEqualTo(HttpStatus.CREATED);
            softly.then(testHelper.getRentalId(rental)).as("rentalId").isNotEmpty();

            log.info("location={}", response.getHeaders().getLocation());
            softly.then(response.getHeaders().getLocation()).as("location").isNotNull();
            softly.assertAll();
        }

        @Test
        void created_rental_has_correct_details() {
            //given
            AutoDTO auto = given_an_auto();
            RenterDTO renter = given_a_renter();
            TimePeriod timePeriod = given_a_time_period();
            RentalDTO proposal = given_a_proposal(auto, renter, timePeriod);
            BigDecimal expectedAmount = auto.getDailyRate().multiply(new BigDecimal(timePeriod.getDays()));
            //when
            ResponseEntity<RentalDTO> response = testHelper.createContract(proposal);
            RentalDTO rental = response.getBody();
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(response.getStatusCode()).as("statusCode").isEqualTo(HttpStatus.CREATED);
            //client-supplied
            softly.then(testHelper.getRentalId(rental)).as("rentalId").isNotEmpty();
            softly.then(testHelper.getAutoId(rental)).as("autoId").isEqualTo(auto.getId());
            softly.then(testHelper.getRenterId(rental)).as("renterId").isEqualTo(renter.getId());
            softly.then(testHelper.getStartDate(rental)).as("startDate").isEqualTo(timePeriod.getStartDate());
            softly.then(testHelper.getEndDate(rental)).as("endDate").isEqualTo(timePeriod.getEndDate());
            //server-side supplied
            softly.then(testHelper.getAutoMakeModel(rental)).as("makeModel").contains(auto.getMake(), auto.getModel());
            softly.then(testHelper.getRenterName(rental)).as("renterName").contains(renter.getFirstName(), renter.getLastName());
            softly.then(testHelper.getRenterAge(rental)).as("renterAge").isGreaterThanOrEqualTo(21);
            softly.then(testHelper.getAmount(rental)).as("amount").isEqualTo(expectedAmount);

            log.info("location={}", response.getHeaders().getLocation());
            String locationString = Optional.ofNullable(response.getHeaders().getLocation())
                    .map(URI::toString).orElse(null);
            softly.then(locationString)
                    .as("location")
                    .startsWith("http")
                    .endsWith(testHelper.getRentalId(rental));
            softly.assertAll();
        }

        @Test
        void reject_bad_auto_id() {
            //given
            AutoDTO badAutoId = AutoDTO.builder().id("DOES_NOT_EXIST").build();
            RentalDTO proposalWithBadAuto = given_a_proposal(badAutoId, null, null);
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    () -> testHelper.createContract(proposalWithBadAuto));
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(ex.getStatusCode().is2xxSuccessful()).isFalse();
            softly.then(ex.getStatusCode()).isIn(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.BAD_REQUEST);
            softly.then(ex.getResponseBodyAsString()).contains(testHelper.getAutoId(proposalWithBadAuto));
            softly.assertAll();
        }

        @Test
        void reject_bad_renter_id() {
            //given
            RenterDTO badRenterId = RenterDTO.builder().id("DOES_NOT_EXIST").build();
            RentalDTO proposalWithBadRenter = given_a_proposal(null, badRenterId, null);
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    () -> testHelper.createContract(proposalWithBadRenter));
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(ex.getStatusCode().is2xxSuccessful()).isFalse();
            softly.then(ex.getStatusCode()).isIn(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.BAD_REQUEST);
            softly.then(ex.getResponseBodyAsString()).contains(testHelper.getRenterId(proposalWithBadRenter));
            softly.assertAll();
        }

        @Test
        void reject_when_renter_underage() {
            RenterDTO underAgeRenter = given_an_underage_renter();
            RentalDTO proposalTooYoung = given_a_proposal(null, underAgeRenter, null);
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class, () ->
                    testHelper.createContract(proposalTooYoung));
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(ex.getStatusCode().is2xxSuccessful()).isFalse();
            softly.then(ex.getStatusCode()).isIn(HttpStatus.UNPROCESSABLE_ENTITY);
            softly.then(ex.getResponseBodyAsString()).contains("too young");
            softly.assertAll();
        }

        @Test
        void reject_when_create_conflicts() {
            //given
            AutoDTO auto = given_an_auto();
            RenterDTO renter = given_a_renter();
            RentalDTO existingRental = given_a_rental_contract(auto, renter, null);

            AutoDTO sameAuto = auto;
            RenterDTO differentRenter = given_a_different_renter(testHelper.getRenterId(existingRental));
            TimePeriod sameTimePeriod = testHelper.getTimePeriod(existingRental);
            RentalDTO conflictingProposal = given_a_proposal(sameAuto, differentRenter, sameTimePeriod);
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    () -> testHelper.createContract(conflictingProposal),
                    "server did not reject conflicting proposal");
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(ex.getStatusCode().is2xxSuccessful()).isFalse();
            softly.then(ex.getStatusCode()).isIn(HttpStatus.UNPROCESSABLE_ENTITY);
            softly.then(ex.getResponseBodyAsString()).contains("conflict");
            softly.assertAll();
        }
    }

    @Nested
    class existing_rental {
        RentalDTO existingRental;

        @BeforeEach
        void given_an_existing_rental_contract() {
            existingRental = given_a_rental_contract();
        }

        @Test
        void can_get_by_rentalId() {
            String autoId = testHelper.getAutoId(existingRental);
            String renterId = testHelper.getRenterId(existingRental);
            String rentalId = testHelper.getRentalId(existingRental);
            //when
            ResponseEntity<RentalDTO> response = testHelper.getRentalById(rentalId);
            RentalDTO foundRental = response.getBody();
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            softly.then(testHelper.getAutoId(foundRental)).isEqualTo(autoId);
            softly.then(testHelper.getRenterId(foundRental)).isEqualTo(renterId);
            softly.then(foundRental).isEqualTo(existingRental);
            softly.assertAll();
        }

        @Test
        public void can_report_rental_notfound() {
            //given
            String badRentalId = "DOES_NOT_EXIST";
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    () -> testHelper.getRentalById(badRentalId));
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(ex.getStatusCode().is2xxSuccessful()).isFalse();
            softly.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            softly.then(ex.getResponseBodyAsString()).contains(badRentalId);
            softly.assertAll();
        }

        @Test
        void can_modify_rental() {
            //given
            AutoDTO auto = given_an_auto();
            RenterDTO renter = given_a_renter();
            RentalDTO contract = given_a_rental_contract(auto, renter, null);
            String rentalId = testHelper.getRentalId(contract);
            TimePeriod timePeriod0 = testHelper.getTimePeriod(contract);
            TimePeriod timePeriodChange = timePeriod0.slide(2);

            RentalDTO updatedProposal = given_a_proposal(auto, renter, timePeriodChange);
            testHelper.setRentalId(updatedProposal, rentalId);
            //when
            ResponseEntity<RentalDTO> response = testHelper.modifyContract(updatedProposal);
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            RentalDTO updatedRental = response.getBody();
            softly.then(testHelper.getStartDate(updatedRental)).as("startDate").isEqualTo(timePeriodChange.getStartDate());
            softly.then(testHelper.getEndDate(updatedRental)).as("endDate").isEqualTo(timePeriodChange.getEndDate());
            softly.assertAll();
        }

        @Test
        void reject_when_modify_conflicts() {
            //given
            AutoDTO auto = given_an_auto();
            RenterDTO renter = given_a_renter();
            RenterDTO renter2 = given_a_different_renter(renter.getId());
            RentalDTO rental0 = given_a_rental_contract(auto, renter, null);
            String rental0Id = testHelper.getRentalId(rental0);
            given_a_rental_contract(auto, renter2, null);
            TimePeriod timePeriod0 = testHelper.getTimePeriod(rental0);
            TimePeriod timePeriodChange = timePeriod0.slide(2);

            RentalDTO conflictingProposal = given_a_proposal(auto, renter, timePeriodChange);
            testHelper.setRentalId(conflictingProposal, rental0Id);
            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                    () -> testHelper.modifyContract(conflictingProposal),
                    "server did not reject conflicting proposal");
            //then
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(ex.getStatusCode().is2xxSuccessful()).isFalse();
            softly.then(ex.getStatusCode()).isIn(HttpStatus.UNPROCESSABLE_ENTITY);
            softly.then(ex.getResponseBodyAsString()).contains("conflict");
            softly.assertAll();
        }

        @Test
        void can_delete_by_rentalId() {
            //given
            String rentalId = testHelper.getRentalId(existingRental);
            //when
            ResponseEntity<Void> response = testHelper.removeRental(rentalId);
            //then
            then(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
            assertThrows(HttpClientErrorException.NotFound.class, ()->
                    testHelper.getRentalById(testHelper.getRentalId(existingRental)),
                    ()->String.format("deleted rental[%s] may still exist", rentalId));
        }
    }

    @Nested
    public class can_query_to {
        private final List<RentalDTO> populatedRentals = new ArrayList<>();
        private AutoDTO populatedAuto;
        @BeforeEach
        void init() {
            testHelper.removeAllRentals();
            populatedAuto = given_an_auto();
            renters.forEach(renter->{
                RentalDTO rental = given_a_rental_contract(populatedAuto, renter, null);
                populatedRentals.add(rental);
            });
        }

        @Test
        void get_all_rentals() {
            //when
            List<RentalDTO> rentals = testHelper.findRentalsBy(SearchParams.builder().build());
            //then
            log.info("returned {} rentals", rentals.size());
            then(rentals).hasSize(populatedRentals.size());
        }

        @Test
        void can_delete_all_rentals() {
            //when
            ResponseEntity<Void> response = testHelper.removeAllRentals();
            //then
            then(response.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);
            List<RentalDTO> found = testHelper.findRentalsBy(SearchParams.builder().build());
            then(found).isEmpty();
        }

        @Test
        void get_all_rentals_paged() {
            int pageNumber = 0;
            int pageSize = 1;
            List<RentalDTO> page;
            List<RentalDTO> allResults = new ArrayList<>();
            do {
                //when
                page = testHelper.findRentalsBy(SearchParams.builder()
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .build());
                //then
                log.info("returned {} rentals", page.size());
                then(page).hasSizeBetween(0, pageSize);
                allResults.addAll(page);
                pageNumber += 1;
            } while (!page.isEmpty());

            then(allResults.size()).isEqualTo(populatedRentals.size());
        }

        @Test
        void find_by_autoId() {
            //given
            RentalDTO targetRental = populatedRentals.get(0);
            String targetAutoId = testHelper.getAutoId(targetRental);
            //when
            List<RentalDTO> rentals = testHelper
                    .findRentalsBy(SearchParams.builder()
                            .autoId(targetAutoId)
                            .build());
            //then
            then(rentals).hasSize(rentals.size());
            RentalDTO foundRental = rentals.get(0);
            String foundAutoId = testHelper.getAutoId(foundRental);
            then(foundAutoId).isEqualTo(targetAutoId);
        }

        @Test
        void find_by_renterId() {
            //given
            final RenterDTO targetRenter = renters.get(1);
            final String targetRenterId = targetRenter.getId();
            long expectedCount = populatedRentals.stream()
                    .filter(rental->StringUtils.equals(targetRenterId, testHelper.getRenterId(rental)))
                    .count();
            //when
            List<RentalDTO> rentals = testHelper
                    .findRentalsBy(SearchParams.builder()
                            .renterId(targetRenterId)
                            .build());
            //then
            log.info("expectedCount={}, actualCount={}", expectedCount, rentals.size());
            then(rentals).hasSize((int)expectedCount);
            RentalDTO foundRental = rentals.get(0);
            String foundRenterId = testHelper.getRenterId(foundRental);
            then(foundRenterId).isEqualTo(targetRenterId);
        }

        @Test
        void find_rental_by_properties() {
            //given
            RentalDTO targetRental = populatedRentals.get(0);
            String rentalId = testHelper.getRentalId(targetRental);
            String autoId = testHelper.getAutoId(targetRental);
            TimePeriod timePeriod = testHelper.getTimePeriod(targetRental);
            SearchParams search = SearchParams.builder()
                    .autoId(autoId)
                    .timePeriod(timePeriod)
                    .build();
            //when
            List<RentalDTO> rentals = testHelper.findRentalsBy(search);
            //then
            then(rentals).hasSize(1);
            RentalDTO foundRental = rentals.get(0);
            then(testHelper.getRentalId(foundRental)).isEqualTo(rentalId);
        }

        @Test
        void find_rental_by_overlap() {
            //given
            RentalDTO targetRental = populatedRentals.get(1);
            String autoId = testHelper.getAutoId(targetRental);
            TimePeriod timePeriod = testHelper.getTimePeriod(targetRental);
            List<String> expectedIds = IntStream.range(0, 2)
                    .mapToObj(i -> testHelper.getRentalId(populatedRentals.get(i)))
                    .toList();
            SearchParams search = SearchParams.builder()
                    .autoId(autoId)
                    .timePeriod(timePeriod.slide(-1 * (timePeriod.getDays()/2)))
                    .build();
            //when
            List<RentalDTO> rentals = testHelper.findRentalsBy(search);
            //then
            log.info("search criteria: {}", search);
            log.info("existing rentals: {}", populatedRentals.stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator())));
            then(rentals).hasSize(2);
            List<String> foundIds = rentals.stream().map(r -> testHelper.getRentalId(r)).toList();
            then(foundIds).containsExactlyInAnyOrder(expectedIds.toArray(new String[0]));
        }

        @Test
        void find_by_startDate() {
            //given
            LocalDate startDate = testHelper.getStartDate(populatedRentals.get(0));
            LocalDate endDate = testHelper.getEndDate(populatedRentals.get(1));
            //when
            List<RentalDTO> rentals = testHelper
                    .findRentalsBy(SearchParams.builder()
                            .timePeriod(new TimePeriod(startDate, endDate))
                            .build());
            //then
            log.info("expectedCount={}, actualCount={}", 2, rentals.size());
            then(rentals).hasSize(2);
        }

        @Test
        void find_by_not_found() {
            //when
            List<RentalDTO> rentals = testHelper.findRentalsBy(SearchParams.builder()
                            .autoId("DOES_NOT_EXIST")
                            .renterId("DOES_NOT_EXIST")
                            .build());
            //then
            then(rentals).hasSize(0);
        }
    }

    @Nested
    public class future_rentals {

        @Test
        void accept_future_contract_when_of_age() {
            RenterDTO futureRenter = given_an_underage_renter(Period.ofYears(3).plusDays(1));
            assertThat(futureRenter.getDob()).isAfter(LocalDate.now().minusYears(minAge));

            LocalDate startOfAge = futureRenter.getDob().plusYears(minAge);
            TimePeriod futureTimePeriod = new TimePeriod(startOfAge, 7);
            RentalDTO futureProposal = given_a_proposal(null, futureRenter, futureTimePeriod);
            TimePeriod futurePeriod = testHelper.getTimePeriod(futureProposal);
            assertThat(Period.between(futureRenter.getDob(),futurePeriod.getStartDate())).hasYears(minAge);

            //when
            ResponseEntity<RentalDTO> response = Assertions.assertDoesNotThrow(() ->
                    testHelper.createContract(futureProposal),
                    "future proposal");

            //then
            then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            RentalDTO futureContract = response.getBody();
            futurePeriod = testHelper.getTimePeriod(futureContract);
            then(Period.between(futureRenter.getDob(),futurePeriod.getStartDate())).hasYears(minAge);
        }

        @Test
        void reject_future_contract_change_when_too_young() {
            RenterDTO futureRenter = given_an_underage_renter(Period.ofYears(3).plusDays(1));
            LocalDate startOfAge = futureRenter.getDob().plusYears(minAge);
            TimePeriod ofAgeTimePeriod = new TimePeriod(startOfAge, 7);
            AutoDTO auto = given_an_auto();
            RentalDTO futureContract = given_a_rental_contract(auto, futureRenter, ofAgeTimePeriod);
            String rentalId = testHelper.getRentalId(futureContract);
            TimePeriod originalTimePeriod = testHelper.getTimePeriod(futureContract);

            TimePeriod tooYoungTimePeriod = ofAgeTimePeriod.slide(Period.ofMonths(1).negated());
            RentalDTO tooYoungProposal = given_a_proposal(auto, futureRenter, tooYoungTimePeriod);
            testHelper.setRentalId(tooYoungProposal, rentalId);

            //when
            HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class, () ->
                    testHelper.modifyContract(tooYoungProposal),
                    ()->String.format("{%s not rejected for change to %s, startDate age=%s",
                            futureRenter,
                            tooYoungTimePeriod,
                            Period.between(futureRenter.getDob(),tooYoungTimePeriod.getStartDate())
                            ));

            //then
            then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            then(ex).hasMessageContaining("too young");

            //then
            RentalDTO storedRental = testHelper.getRental(futureContract).getBody();
            TimePeriod storedTimePeriod = testHelper.getTimePeriod(storedRental);
            then(storedTimePeriod)
                    .as("change rejected, but stored not equal to original")
                    .isEqualTo(originalTimePeriod);
        }

    }

    @Nested
    public class date_query {
        private RentalDTO existingRental;
        private TimePeriod existingTimePeriod;

        @BeforeEach
        void init() {
            testHelper.removeAllRentals();
            existingRental = given_a_rental_contract(null, null, null);
            existingTimePeriod = testHelper.getTimePeriod(existingRental);
        }
        
        @Test
        void does_not_match_timeperiod_before() {
            //given
            Period prior = Period.ofDays(existingTimePeriod.getDays()).plusDays(1).negated();
            TimePeriod before = existingTimePeriod.slide(prior);
            //when
            List<RentalDTO> matches = testHelper.findRentalsBy(SearchParams.builder()
                                            .timePeriod(before)
                                            .build());
            //then
            log.info("existing timePeriod: {}", existingTimePeriod);
            log.info("{} found {} matches {}", before, matches.size(), matches);
            then(matches).isEmpty();
        }

        @Test
        void does_match_timeperiod_overlap() {
            //given
            TimePeriod before = existingTimePeriod.slide(-1 * (existingTimePeriod.getDays()/2));
            //when
            List<RentalDTO> matches = testHelper.findRentalsBy(SearchParams.builder()
                    .timePeriod(before)
                    .build());
            //then
            log.info("existing timePeriod: {}", existingTimePeriod);
            log.info("{} found {} matches {}", before, matches.size(), matches);
            then(matches).contains(existingRental);
        }

        @Test
        void does_not_match_timeperiod_after() {
            //given
            TimePeriod before = existingTimePeriod.next();
            //when
            List<RentalDTO> matches = testHelper.findRentalsBy(SearchParams.builder()
                    .timePeriod(before)
                    .build());
            //then
            log.info("existing timePeriod: {}", existingTimePeriod);
            log.info("{} found {} matches {}", before, matches.size(), matches);
            then(matches).isEmpty();
        }
    }
}
